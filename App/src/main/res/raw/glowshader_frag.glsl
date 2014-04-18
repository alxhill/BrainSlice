precision mediump float;

uniform vec3 centre;

void main()
{
	vec2 dist = gl_FragCoord.xy - centre.xy;
	
	dist.y += 20.0;
	
	float size = 300.0;
	
	float len = length(dist);
	
	len /= size;
	
	len = 1.0 - len;
	
	if(len < 0.0)
		discard;
	
	len*=len;
	
	if(len < 0.0)
		len = 0.0;
	
    gl_FragColor = vec4(len, len, len, 1.0);
}