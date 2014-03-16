precision mediump float;

uniform int transparent;

void main ()
{
	float att = 1.0;
	if(transparent==1)
		gl_FragColor = vec4(0.0, att, 0.0, 0.0);
	else
		gl_FragColor = vec4(att, att, att, 0.0);
}