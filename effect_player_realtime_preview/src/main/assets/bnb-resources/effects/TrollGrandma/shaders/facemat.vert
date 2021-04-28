#include <bnb/glsl.vert>
#include <bnb/decode_int1010102.glsl>
#define bnb_IDX_OFFSET 0
#ifdef BNB_VK_1
#define gl_VertexID gl_VertexIndex
#define gl_InstanceID gl_InstanceIndex
#endif

#define GLFX_USE_UVMORPH

BNB_LAYOUT_LOCATION(0) BNB_IN vec3 attrib_pos;
BNB_LAYOUT_LOCATION(3) BNB_IN vec2 attrib_uv;
#ifndef BNB_GL_ES_1
BNB_LAYOUT_LOCATION(4) BNB_IN uvec4 attrib_bones;
#else
BNB_LAYOUT_LOCATION(4) BNB_IN vec4 attrib_bones;
#endif
#ifndef GLFX_1_BONE
BNB_LAYOUT_LOCATION(5) BNB_IN vec4 attrib_weights;
#endif



BNB_DECLARE_SAMPLER_2D(0, 1, bnb_BONES);

#ifdef GLFX_USE_UVMORPH

BNB_DECLARE_SAMPLER_2D(2, 3, bnb_UVMORPH);
#ifdef GLFX_USE_BG

BNB_DECLARE_SAMPLER_2D(4, 5, bnb_STATICPOS);
#endif
#endif

#ifdef GLFX_USE_AUTOMORPH

BNB_DECLARE_SAMPLER_2D(6, 7, bnb_MORPH);
vec2 glfx_morph_coord( vec3 v )
{
    const float half_angle = radians(104.);
    const float y0 = -110.;
    const float y1 = 112.;
    float x = atan( v.x, v.z )/half_angle;
    float y = ((v.y-y0)/(y1-y0))*2. - 1.;
    return vec2(x,y);
}
#ifndef GLFX_AUTOMORPH_BONE
vec3 glfx_auto_morph( vec3 v )
{
    vec2 morph_uv = glfx_morph_coord(v)*0.5 + 0.5;
    vec3 translation = BNB_TEXTURE_2D(BNB_SAMPLER_2D(bnb_MORPH), morph_uv ).xyz;
    return v + translation;
}
#else
vec3 glfx_auto_morph_bone( vec3 v, mat3x4 m )
{
    vec2 morph_uv = glfx_morph_coord(vec3(m[0][3],m[1][3],m[2][3]))*0.5 + 0.5;
    vec3 translation = BNB_TEXTURE_2D(BNB_SAMPLER_2D(bnb_MORPH), morph_uv ).xyz;
    return v + translation;
}
#endif
#endif

mat3x4 get_bone( uint bone_idx, float k )
{
    float bx = float( int(bone_idx)*3 );
    vec2 rts = 1./vec2(textureSize(BNB_SAMPLER_2D(bnb_BONES),0));
    return mat3x4( 
        BNB_TEXTURE_2D(BNB_SAMPLER_2D(bnb_BONES), (vec2(bx,k)+0.5)*rts ),
        BNB_TEXTURE_2D(BNB_SAMPLER_2D(bnb_BONES), (vec2(bx+1.,k)+0.5)*rts ),
        BNB_TEXTURE_2D(BNB_SAMPLER_2D(bnb_BONES), (vec2(bx+2.,k)+0.5)*rts ) );
}

void main()
{
    mat3x4 m = get_bone( attrib_bones[0], bnb_ANIMKEY );
#ifndef GLFX_1_BONE
    if( attrib_weights[1] > 0. )
    {
        m = m*attrib_weights[0] + get_bone( attrib_bones[1], bnb_ANIMKEY )*attrib_weights[1];
        if( attrib_weights[2] > 0. )
        {
            m += get_bone( attrib_bones[2], bnb_ANIMKEY )*attrib_weights[2];
            if( attrib_weights[3] > 0. )
                m += get_bone( attrib_bones[3], bnb_ANIMKEY )*attrib_weights[3];
        }
    }
#endif

    vec3 vpos = attrib_pos;

#ifdef GLFX_USE_UVMORPH
    vec2 flip_uv = vec2( attrib_uv.x, 1. - attrib_uv.y );
    vec3 translation = BNB_TEXTURE_2D(BNB_SAMPLER_2D(bnb_UVMORPH),flip_uv).xyz;
#ifdef GLFX_UVMORPH_Z_UP
    vpos += vec3(translation.x,-translation.z,translation.y);
#else
    vpos += translation;
#endif
#endif

    vpos = vec4(vpos,1.)*m;

#ifdef GLFX_USE_AUTOMORPH
#ifndef GLFX_AUTOMORPH_BONE
    vpos = glfx_auto_morph( vpos );
#else
    vpos = glfx_auto_morph_bone( vpos, m );
#endif
#endif

    gl_Position = bnb_MVP * vec4(vpos,1.);
}