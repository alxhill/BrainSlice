uniform mat4 modelViewMatrix;
uniform mat4 modelViewProjectionMatrix;

uniform vec4 additionalColor;
uniform vec4 ambientColor;

uniform vec3 lightPositions[8];

attribute vec4 position;
attribute vec3 normal;
attribute vec4 tangent;
attribute vec2 texture0;

varying vec3 lightVec[2]; 
varying vec3 eyeVec;
varying vec2 texCoord;					 

void main(void)
{
	texCoord = texture0.xy;
	
	vec3 n = normalize(modelViewMatrix * vec4(normal,0.0)).xyz;
	vec3 t = normalize(modelViewMatrix * vec4(tangent.xyz, 0.0)).xyz;
	
	vec3 b = tangent.w*cross(n, t);
	
	/*
	vec3 c1 = cross(normal, vec3(0.0, 0.0, 1.0)); 
	vec3 c2 = cross(normal, vec3(0.0, 1.0, 0.0));
	
	vec3 vTangent=c1;
	if (length(c2)>length(vTangent)) {
		vTangent=c2;
	}
	
	vTangent = normalize(vTangent);
	vec3 t = normalize(modelViewMatrix * vec4(vTangent,0.0)).xyz;
	vec3 b = cross(n, t);
	*/
	
	vec3 vVertex = vec3(modelViewMatrix * position);
	vec3 tmpVec = lightPositions[0].xyz - vVertex;

	vec3 lv;
	vec3 ev;

	lv.x = dot(tmpVec, t);
	lv.y = dot(tmpVec, b);
	lv.z = dot(tmpVec, n);

	lightVec[0]=lv;

	tmpVec = vVertex*-1.0;
	eyeVec.x = dot(tmpVec, t);
	eyeVec.y = dot(tmpVec, b);
	eyeVec.z = dot(tmpVec, n);
	
	/*
	tmpVec = lightPositions[1].xyz - vVertex;

	lv.x = dot(tmpVec, t);
	lv.y = dot(tmpVec, b);
	lv.z = dot(tmpVec, n);

	lightVec[1]=lv;
	*/
	
	gl_Position = modelViewProjectionMatrix * position;
}
