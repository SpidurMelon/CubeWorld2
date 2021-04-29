package nl.game.objects;

import org.joml.Vector3f;

import nl.game.util.RenderUtils;

public class Line {
	public Vector3f point1, point2;
	public Line() {
		this.point1 = new Vector3f();
		this.point2 = new Vector3f();
	}
	public Line(Vector3f point1, Vector3f point2) {
		this.point1 = point1;
		this.point2 = point2;
	}
	public Vector3f partWayPoint(float percentage) {
		Vector3f result = new Vector3f();
		float dx = point2.x-point1.x;
		float dy = point2.y-point1.y;
		float dz = point2.z-point1.z;
		point1.add(dx*percentage, dy*percentage, dz*percentage, result);
		return result;
	}
	public boolean intersects(HitBox box) {
		float tx1 = (box.x - point1.x)/(point2.x-point1.x);
		float tx2 = (box.getMaxX() - point1.x)/(point2.x-point1.x);
		float ty1 = (box.y - point1.y)/(point2.y-point1.y);
		float ty2 = (box.getMaxY() - point1.y)/(point2.y-point1.y);
		float tz1 = (box.z - point1.z)/(point2.z-point1.z);
		float tz2 = (box.getMaxZ() - point1.z)/(point2.z-point1.z);

		float tmin = Math.max(Math.max(Math.min(tx1, tx2), Math.min(ty1, ty2)), Math.min(tz1, tz2));
		float tmax = Math.min(Math.min(Math.max(tx1, tx2), Math.max(ty1, ty2)), Math.max(tz1, tz2));

		if (tmax < 0) {
		    return false;
		}

		if (tmin > tmax) {
		    return false;
		}

		return true;
	}
	public Vector3f intersection(HitBox box) {
		return intersection(box, 0);
	}
	public Vector3f intersection(HitBox box, float extraLength) {
		float tx1 = (box.x - point1.x)/(point2.x-point1.x);
		float tx2 = (box.getMaxX() - point1.x)/(point2.x-point1.x);
		float ty1 = (box.y - point1.y)/(point2.y-point1.y);
		float ty2 = (box.getMaxY() - point1.y)/(point2.y-point1.y);
		float tz1 = (box.z - point1.z)/(point2.z-point1.z);
		float tz2 = (box.getMaxZ() - point1.z)/(point2.z-point1.z);

		float tmin = Math.max(Math.max(Math.min(tx1, tx2), Math.min(ty1, ty2)), Math.min(tz1, tz2));
		float tmax = Math.min(Math.min(Math.max(tx1, tx2), Math.max(ty1, ty2)), Math.max(tz1, tz2));

		if (tmax < 0) {
		    return null;
		}

		if (tmin > tmax) {
		    return null;
		}
		return partWayPoint(tmin+extraLength);
	}
}
