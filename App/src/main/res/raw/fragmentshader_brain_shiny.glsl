precision mediump float;

varying vec3 n;
varying vec3 vertex_to_light_vector;

uniform int transparent;

uniform vec3 cameraPos;

uniform vec3 lightPositions[8];

void main()
{
    // Defining The Material Colors
    const vec4 AmbientColor = vec4(0.45, 0.3, 0.3, 1.0);
    const vec4 DiffuseColor = vec4(0.62, 0.4, 0.4, 1.0);
    const vec4 specularColor = vec4(0.2, 0.2, 0.2, 1.0);

    // Scaling The Input Vector To Length 1
    vec3 normalized_normal = normalize(n);
    vec3 normalized_vertex_to_light_vector = normalize(vertex_to_light_vector);

    vec3 H = normalize(lightPosition[0] + cameraPos);

    float p = dot(normalized_normal, H);
    float npow = 0.5;

    // Calculating The Diffuse Term And Clamping It To [0;1]
    float DiffuseTerm = clamp(dot(normalized_normal, normalized_vertex_to_light_vector), 0.0, 1.0);

    //vec3

    vec3 col = (AmbientColor + DiffuseColor * DiffuseTerm).xyz + specularColor.xyz*pow(p, npow);

    // Calculating The Final Color
    gl_FragColor = vec4(col, 1.0);
}





/*precision mediump float;

 uniform int transparent;

 varying vec4 lightVec[2];
 varying vec4 eyeVec;

 void main ()
 {
 	float att = 1.0;

 	//float distSqr = dot(lightVec[0], lightVec[0]);
 	//float att = clamp(sqrt(distSqr), 0.0, 1.0);
 	//vec3 lVec = lightVec[0] * inversesqrt(distSqr);

 	//vec4 vDiffuse = vec4(1.0. 1.0, 1.0, 1.0);

 	//float specular = pow(clamp(dot(-lVec, vVec), 0.0, 1.0), 0.85);
 	//vec4 vSpecular = vec4(specularColors[0],0) * specular;

 	//gl_FragColor = (vAmbient*base + vDiffuse*base + vSpecular) * att*2.0;

 	gl_FragColor = gl_Normal;
 }*/