//-------------------------------------------------------------------------------------------------------
// VST Plug-Ins SDK
// Version 2.4		$Date: 2006/11/13 09:08:27 $
//
// Category     : VST 2.x SDK Samples
// Filename     : RadioVST.h
// Created by   : Steinberg Media Technologies
// Description  : Stereo plugin which applies Gain [-oo, 0dB]
//
// © 2006, Steinberg Media Technologies, All Rights Reserved
//-------------------------------------------------------------------------------------------------------

#ifndef __RadioVST__
#define __RadioVST__

#include "public.sdk/source/vst2.x/audioeffectx.h"
#include "RadioEffect.h"

//-------------------------------------------------------------------------------------------------------
class RadioVST : public AudioEffectX
{
public:
	RadioVST (audioMasterCallback audioMaster);
	~RadioVST ();

	virtual void setBlockSize (VstInt32 blockSize);

	// Processing
	virtual void processReplacing (float** inputs, float** outputs, VstInt32 sampleFrames);

	// Program
	virtual void setProgramName (char* name);
	virtual void getProgramName (char* name);

	// Parameters
	virtual void setParameter (VstInt32 index, float value);
	virtual float getParameter (VstInt32 index);
	virtual void getParameterLabel (VstInt32 index, char* label);
	virtual void getParameterDisplay (VstInt32 index, char* text);
	virtual void getParameterName (VstInt32 index, char* text);

	virtual bool getEffectName (char* name);
	virtual bool getVendorString (char* text);
	virtual bool getProductString (char* text);
	virtual VstInt32 getVendorVersion ();

    virtual void rad2string(float value, char* text, VstInt32 maxLen);	

protected:
	char programName[kVstMaxProgNameLen + 1];

	RadioEffect m_effect;
};

#endif
