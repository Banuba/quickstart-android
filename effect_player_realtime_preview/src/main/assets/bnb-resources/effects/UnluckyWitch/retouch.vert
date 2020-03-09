#version 300 es

#define TEETH_WHITENING
#define teethWhiteningCoeff 1.0
#define EYES_WHITENING
#define eyesWhiteningCoeff 0.5
#define EYES_HIGHLIGHT
#define SOFT_LIGHT_LAYER
#define NORMAL_LAYER
#define SOFT_SKIN
#define skinSoftIntensity 0.7
#define SHARPEN_TEETH
#define teethSharpenIntensity 0.2
#define SHARPEN_EYES
#define eyesSharpenIntensity 0.3
#define PSI 0.1

layout( location = 0 ) in vec3 attrib_pos;
layout( location = 1 ) in vec2 attrib_uv;
layout( location = 3 ) in vec4 attrib_red_mask;

layout(std140) uniform glfx_GLOBAL
{
    mat4 glfx_MVP;
    mat4 glfx_PROJ;
    mat4 glfx_MV;

    vec4 glfx_QUAT;

    vec4 js_is_use_uvh;
};

out vec3 maskColor;
out vec4 var_uv_bg_uv;

invariant gl_Position;

#ifdef GLFX_OCCLUSION
layout(std140) uniform glfx_OCCLUSION_DATA
{
    vec4 glfx_OCCLUSION_RECT;
};
out vec2 glfx_OCCLUSION_UV;
#endif

out float var_is_use_uvh;

void main()
{
    gl_Position = glfx_MVP * vec4( attrib_pos, 1. );
    maskColor = attrib_red_mask.xyz;
    vec2 bg_uv  = (gl_Position.xy / gl_Position.w) * 0.5 + 0.5;
    var_uv_bg_uv = vec4(attrib_uv,bg_uv);

    var_is_use_uvh = js_is_use_uvh.x;
#ifdef GLFX_OCCLUSION
    glfx_OCCLUSION_UV = (gl_Position.xy / gl_Position.w - glfx_OCCLUSION_RECT.xy) / glfx_OCCLUSION_RECT.zw;
    glfx_OCCLUSION_UV = glfx_OCCLUSION_UV * 0.5 + 0.5;
    glfx_OCCLUSION_UV.y = 1.0 - glfx_OCCLUSION_UV.y;
#endif
}