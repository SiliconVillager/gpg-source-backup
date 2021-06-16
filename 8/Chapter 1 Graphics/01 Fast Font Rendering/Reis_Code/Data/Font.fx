// Font.fx:
// Created by: Aurelio Reis


texture g_Texture;

float4 g_f4DebugBatchColor;

int g_iConstArrays;
bool g_bAA;

float2 g_f2TexelSize;
float2 g_f2InvScreenSize;

//#define SKIP_CONSTARRAYS

// The attribute arrays.
#ifndef SKIP_CONSTARRAYS
float4 g_f4PositionArray[ MAX_CONST_ARRAY_QUADS ];
float4 g_f4ColorsArray[ MAX_CONST_ARRAY_QUADS ];
float4 g_f4TCArray[ MAX_CONST_ARRAY_QUADS ];
#endif

sampler TextureSampler = sampler_state
{
    Texture = <g_Texture>;
    MipFilter = LINEAR;
    MinFilter = LINEAR;
    MagFilter = LINEAR;
};


struct VS_INPUT_STD
{
	float3 OffsetXY_IndexZ		: POSITION;

	// Used for constant array instanced data.
	float3 Index				: POSITION2;

	// Used for normal instanced data.
	float4 PosSize				: POSITION1;
	float4 TexDims				: TEXCOORD0;
	float4 Color				: COLOR0;
};

struct VS_OUTPUT_STD
{
	float4 Position				: POSITION;
	float4 Color				: COLOR;
	float2 TC0					: TEXCOORD0;

	// For AA.
	float4 TC1_2				: TEXCOORD1;
	float4 TC3_4				: TEXCOORD2;

};


//////////////////////////////////////////////////////////////////////////
// RenderGPUQuadVS
//////////////////////////////////////////////////////////////////////////

VS_OUTPUT_STD RenderGPUQuadVS( VS_INPUT_STD IN, uniform bool bUsesAA )
{
	VS_OUTPUT_STD OUT;
	
	float4 f4PositionArray;
	float4 f4TcArray;

	if ( g_iConstArrays == 0 )
	{
		f4PositionArray = IN.PosSize;
		f4TcArray = IN.TexDims;
		OUT.Color = IN.Color / ( bUsesAA && g_bAA ? 5.0f : 1.0f ) * g_f4DebugBatchColor;
	}
#ifndef SKIP_CONSTARRAYS
	else
	{
		int iIndex;

		// The index for this vertex into the attribute arrays.
		if ( g_iConstArrays == 1 )
		{
			iIndex = (int)IN.OffsetXY_IndexZ.z;
		}
		else
		{
			iIndex = (int)IN.Index.z;
		}

		f4PositionArray = g_f4PositionArray[ iIndex ];
		f4TcArray = g_f4TCArray[ iIndex ];
		OUT.Color = g_f4ColorsArray[ iIndex ] / ( bUsesAA && g_bAA ? 5.0f : 1.0f ) * g_f4DebugBatchColor;
	}
#endif

	//////////////////////////////////////////////////////////////////////////
	// Calculate the the new vertex position (i.e. expand the quad).
	//////////////////////////////////////////////////////////////////////////

	float2 pos = f4PositionArray.xy;
	float2 size = f4PositionArray.zw * IN.OffsetXY_IndexZ.xy;
	OUT.Position = float4( pos + size, 0.0f, 1.0f );
	
	//////////////////////////////////////////////////////////////////////////
	// Calculate the texture coordinate from the uv position and uv size.
	//////////////////////////////////////////////////////////////////////////

	float2 f2TC = f4TcArray.zw * IN.OffsetXY_IndexZ.xy;	// size
	f2TC = f4TcArray.xy + f2TC;
	
	OUT.TC0 = f2TC;

	//////////////////////////////////////////////////////////////////////////
	// Calculate the filtering texture coordinates.
	//////////////////////////////////////////////////////////////////////////

	if ( bUsesAA && g_bAA )
	{
		OUT.TC1_2 = float4( f2TC.x - g_f2TexelSize.x, f2TC.y - g_f2TexelSize.y,
							f2TC.x + g_f2TexelSize.x, f2TC.y - g_f2TexelSize.y );
		OUT.TC3_4 = float4( f2TC.x + g_f2TexelSize.x, f2TC.y + g_f2TexelSize.y,
							f2TC.x - g_f2TexelSize.x, f2TC.y + g_f2TexelSize.y );
	}
	else
	{
		OUT.TC1_2 = 0.0f;
		OUT.TC3_4 = 0.0f;
	}

    return OUT;
}


//////////////////////////////////////////////////////////////////////////
// RenderSimpleTexAaPS
//////////////////////////////////////////////////////////////////////////

float4 RenderSimpleTexFontPS( VS_OUTPUT_STD IN ) : COLOR
{ 
	if ( g_bAA )
	{
		float4 f4Sample0 = tex2D( TextureSampler, IN.TC0 );
		float4 f4Sample1 = tex2D( TextureSampler, IN.TC1_2.xy );
		float4 f4Sample2 = tex2D( TextureSampler, IN.TC1_2.zw );
		float4 f4Sample3 = tex2D( TextureSampler, IN.TC3_4.xy );
		float4 f4Sample4 = tex2D( TextureSampler, IN.TC3_4.zw );
		
		// NOTE: Implicit divide by 5 (done per-vertex as opposed to per-pixel).
		return ( f4Sample0 + f4Sample1 + f4Sample2 + f4Sample3 + f4Sample4 ) * IN.Color;
	}
	else
	{
		return tex2D( TextureSampler, IN.TC0 ) * IN.Color;
	}
}


//////////////////////////////////////////////////////////////////////////
// RenderSimplePS
//////////////////////////////////////////////////////////////////////////

float4 RenderSimplePS( VS_OUTPUT_STD IN ) : COLOR
{ 
	return IN.Color;
}


//////////////////////////////////////////////////////////////////////////
// RenderSimpleTexPS
//////////////////////////////////////////////////////////////////////////

float4 RenderSimpleTexPS( VS_OUTPUT_STD IN ) : COLOR
{ 
	return tex2D( TextureSampler, IN.TC0 ) * IN.Color;
}


//////////////////////////////////////////////////////////////////////////
// RenderSimpleTexSpherePS
//////////////////////////////////////////////////////////////////////////

float4 RenderSimpleTexCirclePS( VS_OUTPUT_STD IN ) : COLOR
{ 
#if 1
	IN.TC0.xy = IN.TC0.xy * 2.0f - 1.0f;
	float color = saturate( 1.0f - dot( IN.TC0.xy, IN.TC0.xy ) );
	//float color = pow( saturate( 1.0f - dot( IN.TC0.xy, IN.TC0.xy ) ), 10 );
	clip( color - 0.75f );
	return color.xxxx * IN.Color;
#else
	return tex2D( TextureSampler, IN.TC0 ) * IN.Color;
#endif
}


//////////////////////////////////////////////////////////////////////////
// RenderGPUFont
//////////////////////////////////////////////////////////////////////////

technique RenderGPUFont
{
    pass P0
    {          
        VertexShader = compile vs_2_0 RenderGPUQuadVS( true );
        PixelShader  = compile ps_2_0 RenderSimpleTexFontPS();
        
        AlphaBlendEnable = true;
        SrcBlend = SRCALPHA;
        DestBlend = INVSRCALPHA;
        
        ZEnable = false;
    }
}


//////////////////////////////////////////////////////////////////////////
// RenderGPUQuad
//////////////////////////////////////////////////////////////////////////

technique RenderGPUQuad
{
    pass P0
    {          
        VertexShader = compile vs_2_0 RenderGPUQuadVS( false );
        PixelShader  = compile ps_2_0 RenderSimpleTexPS(); 
        
        AlphaBlendEnable = true;
        SrcBlend = SRCALPHA;
        DestBlend = INVSRCALPHA;
        
        ZEnable = false;
		//CullMode = NONE;
		//FillMode = Wireframe;
    }
}


//////////////////////////////////////////////////////////////////////////
// RenderGPUQuadAdd
//////////////////////////////////////////////////////////////////////////

technique RenderGPUQuadAdd
{
    pass P0
    {          
        VertexShader = compile vs_2_0 RenderGPUQuadVS( false );
        PixelShader  = compile ps_2_0 RenderSimpleTexCirclePS();
        
		AlphaBlendEnable = true;
		SrcBlend = ONE;
        DestBlend = ONE;
        
        ZEnable = false;
		//CullMode = NONE;
		//FillMode = Wireframe;
    }
}

