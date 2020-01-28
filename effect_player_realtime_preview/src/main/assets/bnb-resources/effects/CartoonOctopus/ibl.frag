#version 300 es

precision highp float;
precision highp sampler2DArray;
precision highp sampler2DShadow;

//#define GLFX_USE_SHADOW
#define GLFX_TBN
#define GLFX_TEX_MRAO
//#define GLFX_TEX_AO

in vec2 var_uv;
#ifdef GLFX_TBN
in vec3 var_t;
in vec3 var_b;
#endif
in vec3 var_n;
in vec3 var_v;

layout( location = 0 ) out vec4 frag_color;

uniform sampler2D tex_diffuse;
#ifdef GLFX_TBN
uniform sampler2D tex_normal;
#endif
#ifdef GLFX_TEX_MRAO
uniform sampler2D tex_mrao;
#else
uniform sampler2D tex_metallic;
uniform sampler2D tex_roughness;
#endif
#ifdef GLFX_TEX_AO
uniform sampler2D tex_ao;
#endif

#ifdef GLFX_USE_SHADOW
in vec3 var_shadow_coord;
uniform sampler2DShadow glfx_SHADOW;
float glfx_shadow_factor()
{
	const vec2 offsets[] = vec2[](
		vec2( -0.94201624, -0.39906216 ),
		vec2( 0.94558609, -0.76890725 ),
		vec2( -0.094184101, -0.92938870 ),
		vec2( 0.34495938, 0.29387760 )
	);
	float s = 0.;
	for( int i = 0; i != offsets.length(); ++i )
		s += texture( glfx_SHADOW, var_shadow_coord + vec3(offsets[i]/110.,0.1) );
	s *= 0.125;
	return s;
}
#endif

// gamma to linear
vec3 g2l( vec3 g )
{
	return g*(g*(g*0.305306011+0.682171111)+0.012522878);
}

// linear to gamma
vec3 l2g( vec3 l )
{
	vec3 s = sqrt(l);
	vec3 q = sqrt(s);
	return 0.662002687*s + 0.68412206*q - 0.323583601*sqrt(q) - 0.022541147*l;
}

vec3 fresnel_schlick( float prod, vec3 F0 )
{
	return F0 + ( 1. - F0 )*pow( 1. - prod, 5. );
}

vec3 fresnel_schlick_roughness( float prod, vec3 F0, float roughness )
{
	return F0 + ( max( F0, 1. - roughness ) - F0 )*pow( 1. - prod, 5. );
}

float distribution_GGX( float cN_H, float roughness )
{
	float a = roughness*roughness;
	float a2 = a*a;
	float d = cN_H*cN_H*( a2 - 1. ) + 1.;
	return a2/(3.14159265*d*d);
}

float geometry_schlick_GGX( float NV, float roughness )
{
	float r = roughness + 1.;
	float k = r*r/8.;
	return NV/( NV*( 1. - k ) + k );
}

float geometry_smith( float cN_L, float ggx2, float roughness )
{
	return geometry_schlick_GGX( cN_L, roughness )*ggx2;
}

float diffuse_factor( float n_l, float w )
{
	float w1 = 1. + w;
	return pow( max( 0., n_l + w )/w1, w1 );
}

// direction in xyz, lwrap in w
const vec4 lights[] = vec4[]( 
	vec4(0.,0.6,0.8,1.),
	vec4(normalize(vec3(-197.6166,150.,3.151)),1.)
	);
const vec3 radiance[] = vec3[]( 
	vec3(1.,1.,1.)*2.,
	vec3(1.,1.,1.)*0.9*2.
	);

uniform sampler2D tex_brdf;
uniform samplerCube tex_ibl_diff, tex_ibl_spec;

void main()
{
	vec4 base_opacity = texture(tex_diffuse,var_uv);

	//if( base_opacity.w < 0.5 ) discard;

	vec3 base = g2l(base_opacity.xyz);
	float opacity = base_opacity.w;
#ifdef GLFX_TEX_MRAO
	vec2 metallic_roughness = texture(tex_mrao,var_uv).xy;
	float metallic = metallic_roughness.x;
	float roughness = metallic_roughness.y;
#else
	float metallic = texture(tex_metallic,var_uv).x;
	float roughness = texture(tex_roughness,var_uv).x;
#endif

#ifdef GLFX_TBN
	vec3 N = normalize( mat3(var_t,var_b,var_n)*(texture(tex_normal,var_uv).xyz*2.-1.) );
#else
	vec3 N = normalize( var_n );
#endif
	vec3 V = normalize( -var_v );
	float cN_V = max( 0., dot( N, V ) );
	vec3 R = reflect( -V, N );

	//float ggx2 = geometry_schlick_GGX( cN_V, roughness );
	vec3 F0 = mix( vec3(0.04), base, metallic );

	vec3 F = fresnel_schlick_roughness( cN_V, F0, roughness );
	vec3 kD = ( 1. - F )*( 1. - metallic );	  
	
	vec3 diffuse = texture( tex_ibl_diff, N ).xyz * base;
	
	const float MAX_REFLECTION_LOD = 4.0;
	vec3 prefilteredColor = textureLod( tex_ibl_spec, R, roughness * MAX_REFLECTION_LOD ).xyz;
	vec2 brdf = texture( tex_brdf, vec2( cN_V, roughness ) ).wz;	// TODO .xy
	vec3 specular = prefilteredColor * (F * brdf.x + brdf.y);

	vec3 color = (kD*diffuse + specular);

#ifdef GLFX_TEX_AO
	color *= texture(tex_ao,var_uv).x;
#endif

#ifdef GLFX_USE_SHADOW
	color = mix( color, vec3(0.), glfx_shadow_factor() );
#endif

	frag_color = vec4(l2g(color),opacity);
}
