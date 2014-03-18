precision mediump float;

varying vec3 n;
varying vec3 vertex_to_light_vector;

uniform int transparent;

void main()
{
    // Defining The Material Colors


    // Scaling The Input Vector To Length 1
    vec3 normalized_normal = normalize(n);
    vec3 normalized_vertex_to_light_vector = normalize(vertex_to_light_vector);

    normalized_vertex_to_light_vector.y = 0.0;
    normalized_vertex_to_light_vector.z = 0.0;

    // Calculating The Diffuse Term And Clamping It To [0;1]



    vec3 col = (AmbientColor + DiffuseColor * DiffuseTerm).xyz;
    float blend = length(col);

    // Calculating The Final Color
    gl_FragColor = vec4(col, blend);
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