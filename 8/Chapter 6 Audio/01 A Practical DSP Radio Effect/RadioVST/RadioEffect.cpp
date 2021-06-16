#include "RadioEffect.h"
#include <memory>
#include <math.h>
#include <float.h>
#include <algorithm>

// Windows keeps its min and max in xutility. Remember
// to define NOMINMAX.
#ifdef _WIN32 
#include <xutility>
#endif

const size_t kRmsWindow = 64;



RMS::RMS( size_t windowSize )
: m_windowSize( windowSize )
, m_currentPosition( 0 )
, m_fWindowSize( (sample_t)windowSize )
, m_accumulator( 0.0f )
{
	m_window = new sample_t[m_windowSize];
	memset( m_window, 0, sizeof( m_window[0] ) * m_windowSize );
}

void RMS::Process( const sample_t* in, sample_t* out, size_t nSamples )
{

	for( size_t i = 0; i < nSamples; 
		++i, ++m_currentPosition, ++in, ++out )
	{
		//
		// Calculate the current sample's contribution to
		// the mean square
		//
		sample_t s = abs(*in);
		sample_t s2 = s * s;
        sample_t s2m = s2 / m_fWindowSize;

		// subtract the oldest sample's contribution
		m_accumulator -= m_window[ m_currentPosition % m_windowSize ];

		// add in the current sample's contribution
		m_accumulator += s;

		// write the current sample's contribution to the window
		m_window[ m_currentPosition % m_windowSize ] = s;

		// Take the root of the current mean square; this
		// is our output for the current sample
        *out = m_accumulator / m_fWindowSize;
	}

	m_currentPosition %= m_windowSize;
}

RMS::~RMS()
{
	delete m_window;
}

Waveshaper::Waveshaper()
: m_a(0.0f), m_b(0.0f), m_c(0.0f), m_d(0.0f)
, m_threshold( 0.2f )
, m_knee(0.4f)
, m_ratio(0.4f)
{
	computeCoefficients();
}

void Waveshaper::Process( const sample_t* in, sample_t* out, size_t nSamples )
{
	for( size_t i = 0; i < nSamples; ++i, ++in, ++out )
	{
		sample_t x = *in;
        sample_t y;

		sample_t sign = x < 0.0f ? -1.0f : 1.0f;
		x *= sign;

		if( x > m_threshold && x <= (m_threshold + m_knee))
		{
			sample_t x2 = x * x;
			sample_t x3 = x * x2;
			sample_t x4 = x2 * x2;

			y = m_a * x4
				+ m_b * x3
			    + m_c * x2
				+ m_d * x;

		}
        if( x >= m_knee )
        {
            y = ( x - m_knee ) * m_ratio + m_knee;
        }
		else
		{
			y = x;
		}
        *out = y * sign;
	}
}

void Waveshaper::computeCoefficients()
{
	float t = m_threshold;
	float k = m_knee;
	float r = m_ratio;

	float denom = -8*k*t - 3*t*t*t + t*t*t*t - 8*t*k*k -3*k*t*t*t + 2*k*k*t*t + 6*k*k +2*t*t + 11*k*t*t;
	
	if( abs( denom ) > FLT_EPSILON )
	{
		m_a = -0.5f * ( ( -3*k + 4*t - 3 + 3*k*r - 4*t*r + 3*r ) / denom ); 
		m_b = ( 2*k*k*r - 2*k*k + 2*k*r - 2*k - 3*t*t*r + 3*t*t + 2*r - 2 ) / denom;
		m_c = -0.5f * ( t * ( -8*k*k + 8*k*k*r - 9*r*k*t + 9*k*t - 8*k + 8*k*r - 9*t*r + 9*t - 8 + 8*r ) / denom );
		m_d = ( 9*k*t*t - 8*t*k*k + 2*t*t*r + t*t*t*t*r - 3*t*t*t*r + 2*t*t*k*k*r + 2*k*t*t*r - 3*t*t*t*k*r - 8*k*t + 6*k*k ) / denom;
	}
}


BandpassFilter::BandpassFilter()
: m_frequency(0.5f)
, m_q(0.5f)
{
    memset( m_x, 0, sizeof( m_x ) );
    memset( m_y, 0, sizeof( m_y ) );

    computeCoefficients();
}

void BandpassFilter::Process( const sample_t* in, sample_t* out, size_t nSamples )
{
    for( size_t i = 0; i < nSamples; ++i, ++in, ++out )
    {
        sample_t x = *in;
        sample_t y = (m_ab0 * x) + (m_b[0] * m_x[0]) + (m_b[1] * m_x[1])
                - (m_a[0] * m_y[0]) -  (m_a[1] * m_y[1]);

        y = std::max( y, -10.0f );
        y = std::min( 10.0f, y );

        m_x[1] = m_x[0];
        m_x[0] = x;
        m_y[1] = m_y[0];
        m_y[0] = y;
          
        *out = y;
    }
}

void BandpassFilter::computeCoefficients()
{
    sample_t w0 = m_frequency;
    sample_t sin0 = sin(w0);
    sample_t cos0 = cos(w0);

    sample_t alpha = sin0/(2*m_q);


    sample_t a0 = alpha + 1.0f;
    sample_t a1 = -2.0f * cos0;
    sample_t a2 = 1.0f - alpha;
    sample_t b0 = sin0/2.0f;
    sample_t b1 = 0.0f;
    sample_t b2 = -sin0/2.0f;

    m_ab0 = b0 / a0;
    m_a[0] = a1 / a0;
    m_a[1] = a2 / a0;
    m_b[0] = b1 / a0;
    m_b[1] = b2 / a0;
}


RadioEffect::RadioEffect(void)
: m_gainTarget( 1.0f )
, m_rms1( kRmsWindow )
, m_rms2( kRmsWindow )
, m_rmsBuffer1(NULL)
, m_rmsBuffer2(NULL)
{
}

RadioEffect::~RadioEffect(void)
{
	delete m_rmsBuffer1;
    delete m_rmsBuffer2;
}

void RadioEffect::Process( const sample_t* in_, sample_t* out_, size_t nSamples )
{
    const sample_t* in = in_;
    sample_t* out = out_;


	//
	// AGC1
	//
    in = in_;
    out = out_;
    m_rms1.Process( in, m_rmsBuffer1, nSamples );
	for( size_t i = 0; i < nSamples; ++i, ++in, ++out )
	{
		sample_t rms = m_rmsBuffer1[i];
        sample_t shortfall = m_gainTarget - rms;
        sample_t pctShortfall = shortfall / std::max(rms,.1f);
		*out = *in * (1.f + pctShortfall );
	}
    
    //
    // Distortion
    //
    out = out_;
    m_ws.Process( out, out, nSamples );


    //
    // AGC2
    //
    out = out_;
    m_rms2.Process( out, m_rmsBuffer2, nSamples );
    for( size_t i = 0; i < nSamples; ++i, ++out )
    {
        sample_t rms1 = std::min( 1.0f, std::max(m_rmsBuffer1[i],FLT_EPSILON) );
        sample_t rms2 = m_rmsBuffer2[i];

        sample_t shortfall = rms1 - rms2;
        sample_t pctShortfall = shortfall / std::max(rms2,.1f);

        *out = *out * (1.0f + pctShortfall );

        float noise = (float)(~(*(int*)out)) / (float)INT_MAX;
        *out += m_noiseLevel * noise;
    }


    
    //
    // Noise
    //
    out = out_;
    for( size_t i = 0; i < nSamples; ++i, ++out )
    {
        sample_t input = *out; 
        float noise = (float)(~(*(int*)&input)) / (float)INT_MAX;
        *out += m_noiseLevel * noise;
    }

    //
    // Bandlimiting
    //
    out = out_;
    m_bpf.Process( out, out, nSamples );}

void RadioEffect::SetBlockSize( size_t blockSize )
{
    delete m_rmsBuffer1;
    delete m_rmsBuffer2;
	m_rmsBuffer1 = new sample_t[blockSize];
    m_rmsBuffer2 = new sample_t[blockSize];
}

