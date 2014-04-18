precision mediump float;

uniform vec3 centre;

uniform int sw;
uniform int sh;

///480x800 is s2

void main()
{
	vec2 dist = gl_FragCoord.xy - centre.xy;
	
	dist.y += 20.0;
	
	//float size = 300.0;
	
	int isize = sw < sh ? sw : sh;
	
	float fsize = float(isize);
	
	float size = fsize / 1.5;
		
	float len = length(dist);
	
	len /= size;
	
	len = 1.0 - len;
	
	if(len < 0.0)
		len = 0.0;
	
	len *= len;
	
    gl_FragColor = vec4(len, len, len, 1.0);
}