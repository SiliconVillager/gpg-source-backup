uniform extern float4x4 matrixWorld;
uniform extern float4x4 matrixViewProjection;
uniform extern float3 DirToSun;

struct VS_INPUT
{
   float4 position : POSITION0;
   float3 normal : NORMAL0;
   float2 uv : TEXCOORD0;         //alpha UV
   float2 uv2 : TEXCOORD1;        //texture UV
};

struct VS_OUTPUT
{
   float4 position : POSITION0;
   float2 uv : TEXCOORD0;
   float2 uv2 : TEXCOORD1;
   float  shade : TEXCOORD2;
};

VS_OUTPUT Main(VS_INPUT input)
{
   VS_OUTPUT output = (VS_OUTPUT)0;

   // Wolrd/View/Projection transformation
   float4 worldPosition = mul(input.position, matrixWorld);
   output.position = mul(worldPosition, matrixViewProjection);

   //Dynamic Shade Calculation based on sun direction
   output.shade = max(0.0f, dot(normalize(input.normal), DirToSun));
   output.shade = 0.2f + output.shade * 0.8f;

   output.uv = input.uv;
   output.uv2 = input.uv2;

   return output;
}

