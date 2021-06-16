uniform extern float4x4 matrixWorld;
uniform extern float4x4 matrixViewProject;
uniform extern float3 DirToSun;
uniform extern float3 mapSize;

struct VS_INPUT1
{
   float4 position : POSITION0;
   float3 normal : NORMAL0;
   float2 uv : TEXCOORD0;
};

struct VS_OUTPUT1
{
   float4 position : POSITION0;
   float2 uv : TEXCOORD0;
   float2 uv2 : TEXCOORD1;
   float  shade : TEXCOORD2;
};

VS_OUTPUT1 Main(VS_INPUT1 input)
{
   VS_OUTPUT1 output = (VS_OUTPUT1)0;

   // World\View\Projection transformation
   float4 worldPosition = mul(input.position, matrixWorld);
   output.position = mul(worldPosition, matrixViewProject);
   input.normal = mul(input.normal, matrixWorld);

   //Dynamic shade calculation based on sun direction
   output.shade = max(0.0f, dot(normalize(input.normal), DirToSun));
   output.shade = 0.2f + output.shade * 0.8f;

   //Set UV coordinates
   output.uv = input.uv;
   output.uv2 = float2(worldPosition.x / mapSize.x, -worldPosition.z / mapSize.y);

   return output;
}

