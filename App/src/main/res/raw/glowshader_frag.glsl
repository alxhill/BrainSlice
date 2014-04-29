precision mediump float;

uniform vec3 centre;

uniform int sw;
uniform int sh;
uniform float scale;
uniform int is_back;

///480x800 is s2

void main()
{
	/*vec4 background_col = vec4(0.1, 0.1, 0.3, 1.0);
	vec4 glow_col = vec4(1.0, 1.0, 1.0, 1.0);

	vec2 dist = gl_FragCoord.xy - centre.xy;
	
	dist.y += 20.0;*/
	
	//float size = 300.0;
	
	/*int isize = sw < sh ? sw : sh;
	
	float fsize = float(isize);
	
	float size = fsize / 1.0;
		
	float len = length(dist);
	
	len /= size;
	
	//len = 1.0 - len;
	
	if(len < 0.0)
		len = 0.0;
		
	if(len > 1.0)
		len = 1.0;
	
	len *= len;
	
	vec4 col = len*glow_col + (1.0-len)*background_col;*/
	
	vec2 dist = gl_FragCoord.xy - centre.xy;
	dist.y += 20.0;
	
	float val = length(dist);
	float sadjust = scale/0.2;
	val = abs(dist.x/sadjust) + abs(dist.y/sadjust);
	//val /= scale/0.2;
	
	val /= 933.0;
	
	if( val >= 1.0)
		val = 0.999;
		
	val = 1.0 - val;
	
	val *= val;
	
	val /= 1.0;
	
	vec4 col = vec4(1.0/1.2, 1.0/1.2,  1.0/1.2, 1.0);
	
	vec4 background_col = vec4(0.2, 0.2, 0.3, 1.0);
	
	vec4 fcol = vec4(col.xyz*val + background_col.xyz*(1.0-val), 1.0);

	if(is_back == 1)
        gl_FragColor = fcol;
    else
        gl_FragColor = vec4(0.1, 0.1, 0.2, 1.0);
}