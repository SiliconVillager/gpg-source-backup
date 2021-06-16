#pragma once

typedef float sample_t;

class RMS
{
public:
	RMS( size_t windowSize );
	~RMS();

	void Process( const sample_t* in, sample_t* out, size_t nSamples );
private:
	size_t		m_windowSize;
	sample_t	m_fWindowSize;
	sample_t*	m_window;

	sample_t	m_accumulator;

	size_t		m_currentPosition;
};

class Waveshaper
{
public:
	Waveshaper();

	void Process( const sample_t* in, sample_t* out, size_t nSamples );

	sample_t    GetRatio() const            { return m_ratio; }
	void        SetRatio(sample_t val)      { m_ratio = val; computeCoefficients();}
	sample_t    GetThreshold() const        { return m_threshold; }
	void        SetThreshold(sample_t val)  { m_threshold = val; computeCoefficients();}
	sample_t    GetKnee() const             { return m_knee; }
	void        SetKnee(sample_t val)       { m_knee = val; computeCoefficients();}
private:
	// parameters
	sample_t m_ratio;
	sample_t m_threshold;
	sample_t m_knee;
	
	// coefficients
	sample_t m_a, m_b, m_c, m_d;

	void computeCoefficients();
};

class BandpassFilter
{
public:
    BandpassFilter();

    void Process( const sample_t* in, sample_t* out, size_t nSamples );

    sample_t    GetFrequency() const        { return m_frequency; }
    void        SetFrequency(sample_t val)  { m_frequency = val; computeCoefficients();}
    sample_t    GetQ() const                { return m_q; }
    void        SetQ(sample_t val)          { m_q = val; computeCoefficients();}

private:
    sample_t m_frequency; // in radians
    sample_t m_q;

    sample_t m_x[2];
    sample_t m_y[2];
    sample_t m_a[2];
    sample_t m_b[2];
    sample_t m_ab0;

    void computeCoefficients();
};

class RadioEffect
{
public:
	RadioEffect(void);
	~RadioEffect(void);

	void Process( const sample_t* in, sample_t* out, size_t nSamples );
    void SetBlockSize( size_t blockSize );

	sample_t    GetGainTarget() const       { return m_gainTarget; }
	void        SetGainTarget(sample_t val) { m_gainTarget = val; }

	sample_t    GetRatio() const            { return m_ws.GetRatio(); }
	void        SetRatio(sample_t val)      { m_ws.SetRatio(val); }
	sample_t    GetThreshold() const        { return m_ws.GetThreshold(); }
	void        SetThreshold(sample_t val)  { m_ws.SetThreshold(val);}
	sample_t    GetKnee() const             { return m_ws.GetKnee(); }
	void        SetKnee(sample_t val)       { m_ws.SetKnee(val);}

    sample_t    GetFrequency() const        { return m_bpf.GetFrequency(); }
    void        SetFrequency(sample_t val)  { m_bpf.SetFrequency(val);}
    sample_t    GetQ() const                { return m_bpf.GetQ(); }
    void        SetQ(sample_t val)          { m_bpf.SetQ(val);}

    sample_t    GetNoiseLevel() const       { return m_noiseLevel; }
    void        SetNoiseLevel(sample_t val) { m_noiseLevel = val; }
private:
	//
	// AGC 
	//
    RMS m_rms1;
    RMS m_rms2;
    sample_t* m_rmsBuffer1;
    sample_t* m_rmsBuffer2;
	sample_t m_gainTarget;

	//
	// Distortion
	//
	Waveshaper m_ws;

    //
    // Bandlimiting
    //
    BandpassFilter m_bpf;

    //
    // Static Noise
    //
    sample_t m_noiseLevel;
};
