precision mediump float;

uniform vec3 centre;

uniform int sw;
uniform int sh;
uniform float scale;
uniform int is_back;

///480x800 is s2

void main()
{
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
	vec4 background_col = vec4(30.0/255.0, 87.0/255.0, 153.0/255.0, 1.0);
	
	vec4 fcol = vec4(col.xyz*val + background_col.xyz*(1.0-val), 1.0);

	if(is_back == 1)
        gl_FragColor = fcol;
    else
        gl_FragColor = vec4(42.0/255.0, 70.0/255.0, 88.0/255.0, 1.0);
}