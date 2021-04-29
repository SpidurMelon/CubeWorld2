#version 400

in vec3 position;
in float brightness;

uniform mat4 perspectiveTransform;
uniform mat4 worldTransform;

uniform float fogStart;
uniform float fogEnd;
uniform vec4 fogColor;

uniform mat4 entityTransform;
uniform vec3 entityPosition;

uniform vec4 color;
out vec4 color0;

void main() {
	gl_Position = perspectiveTransform * worldTransform * vec4(entityPosition+position, 1);
	
	vec4 vertexWorldPosition = worldTransform * vec4(entityPosition+position, 1);
	float distanceV = pow(vertexWorldPosition[0]*vertexWorldPosition[0]+vertexWorldPosition[1]*vertexWorldPosition[1]+vertexWorldPosition[2]*vertexWorldPosition[2], 0.5);
	
	if ((distanceV > fogStart) && (distanceV < fogEnd)) {
		color0 = (color * min(1-(distanceV-fogStart)/(fogEnd-fogStart), 1)) + (fogColor * min((distanceV-fogStart)/(fogEnd-fogStart), 1));
	} else if (distanceV > fogEnd) {
		color0 = fogColor;
	} else {
		color0 = color;
	}
	
}