precision mediump float;

varying vec3 lightVec[2];
varying vec3 eyeVec;
varying vec2 texCoord;

uniform sampler2D textureUnit0;
uniform sampler2D textureUnit1;

uniform vec3 diffuseColors[8];
uniform vec3 specularColors[8];

uniform vec4 ambientColor;

uniform float invRadius;
uniform float heightScale;

uniform vec3 spherePos;

uniform int isSelected;

void main ()
{
	float dist = length(gl_FragCoord.xy - spherePos.xy);
	float att = clamp(1.0 - 0.07 * dist, 0.0, 1.0);
	if(isSelected==1)
		gl_FragColor = vec4(att, 0.0, 0.0, 0.0);
	else
		gl_FragColor = vec4(0.0, 0.0, att, 0.0);
} 