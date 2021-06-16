//-------------------------------------------------------------------------------------------------------
// VST Plug-Ins SDK
// Version 2.4		$Date: 2006/11/13 09:08:27 $
//
// Category     : VST 2.x SDK Samples
// Filename     : RadioVST.cpp
// Created by   : Steinberg Media Technologies
// Description  : Stereo plugin which applies Gain [-oo, 0dB]
//
// © 2006, Steinberg Media Technologies, All Rights Reserved
//-------------------------------------------------------------------------------------------------------

#include "RadioVST.h"
#include <BaseTsd.h>
#include <boost/function.hpp>
#define _USE_MATH_DEFINES
#include <math.h>
#include <float.h>


struct ParamMap
{
	const char* name;
	const char* label;
	sample_t rangeLo, rangeHi;
	void (RadioVST::*pfnConvert)(float,char*,VstInt32);
	sample_t (RadioEffect::*pfnGet)() const;
	void (RadioEffect::*pfnSet)(sample_t);
};

ParamMap RadioParams[] = 
{
	{ "Gain Tgt",	"dB",   0.0f, 4.0f,     &RadioVST::dB2string,    &RadioEffect::GetGainTarget,    &RadioEffect::SetGainTarget },
	{ "Threshold",	"dB",   0.0f, 1.0f,     &RadioVST::dB2string,    &RadioEffect::GetThreshold,     &RadioEffect::SetThreshold },
	{ "Knee",		"",     0.0f, 1.0f,     &RadioVST::float2string, &RadioEffect::GetKnee,          &RadioEffect::SetKnee },
    { "Ratio",		"",     0.0f, 1.0f,     &RadioVST::float2string, &RadioEffect::GetRatio,         &RadioEffect::SetRatio },
    { "BP freq",	"Hz",   0.01f, M_PI,    &RadioVST::rad2string,   &RadioEffect::GetFrequency,     &RadioEffect::SetFrequency },
    { "BP Q",		"",     .1f, 10.0f,     &RadioVST::float2string, &RadioEffect::GetQ,             &RadioEffect::SetQ },
    { "Noise",		"dB",   0.0f, 1.0f,     &RadioVST::dB2string,    &RadioEffect::GetNoiseLevel,    &RadioEffect::SetNoiseLevel },
};

//-------------------------------------------------------------------------------------------------------
AudioEffect* createEffectInstance (audioMasterCallback audioMaster)
{
	return new RadioVST (audioMaster);
}

//-------------------------------------------------------------------------------------------------------
RadioVST::RadioVST (audioMasterCallback audioMaster)
: AudioEffectX (audioMaster, 1, _countof(RadioParams))	// 1 program, 1 parameter only
{
	setNumInputs (1);		// mono in
	setNumOutputs (1);		// mono out
	setUniqueID ('RVST');	// identify
	canProcessReplacing ();	// supports replacing output

	m_effect.SetGainTarget( 1.0f );			// default to 0 dB
	vst_strncpy (programName, "Default", kVstMaxProgNameLen);	// default program name
}

//-------------------------------------------------------------------------------------------------------
RadioVST::~RadioVST ()
{
	// nothing to do here
}

//-------------------------------------------------------------------------------------------------------
void RadioVST::setProgramName (char* name)
{
	vst_strncpy (programName, name, kVstMaxProgNameLen);
}

//-----------------------------------------------------------------------------------------
void RadioVST::getProgramName (char* name)
{
	vst_strncpy (name, programName, kVstMaxProgNameLen);
}

//-----------------------------------------------------------------------------------------
void RadioVST::setParameter (VstInt32 index, float value)
{
	if( index >=0 && index <= _countof( RadioParams ) )
	{
        value *= (RadioParams[index].rangeHi - RadioParams[index].rangeLo );
        value += RadioParams[index].rangeLo;
		(m_effect.*(RadioParams[index].pfnSet))( value );
	}
}

//-----------------------------------------------------------------------------------------
float RadioVST::getParameter (VstInt32 index)
{
	float value = 0.0f;
	if( index >=0 && index <= _countof( RadioParams ) )
	{
		value = (m_effect.*(RadioParams[index].pfnGet))();
        value -= RadioParams[index].rangeLo;
        value /= (RadioParams[index].rangeHi - RadioParams[index].rangeLo );
	}
	return value;
}

//-----------------------------------------------------------------------------------------
void RadioVST::getParameterName (VstInt32 index, char* label)
{
	if( index >=0 && index <= _countof( RadioParams ) )
	{
		vst_strncpy (label, RadioParams[index].name, kVstMaxParamStrLen);
	}
}

//-----------------------------------------------------------------------------------------
void RadioVST::getParameterDisplay (VstInt32 index, char* text)
{
	float value = 0.0f;
	if( index >=0 && index <= _countof( RadioParams ) )
	{
        value = (m_effect.*(RadioParams[index].pfnGet))();
		(this->*(RadioParams[index].pfnConvert))( value, text, kVstMaxParamStrLen );
	}
}

//-----------------------------------------------------------------------------------------
void RadioVST::getParameterLabel (VstInt32 index, char* label)
{
	if( index >=0 && index <= _countof( RadioParams ) )
	{
		vst_strncpy (label, RadioParams[index].label, kVstMaxParamStrLen);
	}
}

//------------------------------------------------------------------------
bool RadioVST::getEffectName (char* name)
{
	vst_strncpy (name, "Radioizer", kVstMaxEffectNameLen);
	return true;
}

//------------------------------------------------------------------------
bool RadioVST::getProductString (char* text)
{
	vst_strncpy (text, "Radio VST", kVstMaxProductStrLen);
	return true;
}

//------------------------------------------------------------------------
bool RadioVST::getVendorString (char* text)
{
	vst_strncpy (text, "Ian Lewis", kVstMaxVendorStrLen);
	return true;
}

//-----------------------------------------------------------------------------------------
VstInt32 RadioVST::getVendorVersion ()
{ 
	return 1000; 
}

//-----------------------------------------------------------------------------------------
void RadioVST::processReplacing (float** inputs, float** outputs, VstInt32 sampleFrames)
{
    float* in1  =  inputs[0];
    float* out1 = outputs[0];

    m_effect.Process( in1, out1, sampleFrames );
}

//-----------------------------------------------------------------------------------------
void RadioVST::setBlockSize( VstInt32 blockSize )
{
	AudioEffectX::setBlockSize( blockSize );
	m_effect.SetBlockSize( blockSize );
}

void RadioVST::rad2string( float value, char* text, VstInt32 maxLen )
{
    Hz2string( 1.0 / (value / (2.0f * M_PI)), text, maxLen );
}