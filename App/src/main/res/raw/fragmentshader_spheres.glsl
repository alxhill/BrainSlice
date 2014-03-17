precision mediump float;

uniform vec3 spherePos;
uniform int isSelected;

void main ()
{
	float dist = length(gl_FragCoord.xy - spherePos.xy);
	float att = clamp(1.0 - 0.03 * dist, 0.0, 1.0);
	if(isSelected==1)
		gl_FragColor = vec4(0.0, att, 0.0, 0.5);
	else
		gl_FragColor = vec4(att, att, att, 0.5);
}