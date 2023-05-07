package lab6;

public class Coordinate
{

	private int x, y;

	public Coordinate(int x, int y)
	{
		this.setX(x);
		this.setY(y);
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public String toString()
	{
		return "(" + this.getX() + ", " + this.getY() + ")";
	}
	
	public int hashCode()
	{
		return this.getX() * this.getX() + this.getY() * this.getY() * this.getY();
	}

	public boolean equals(Object other)
	{
		if(other instanceof Coordinate)
		{
			return this.getX() == ((Coordinate)other).getX() && this.getY() == ((Coordinate)other).getY();
		}
		return false;
	}

	public double euclideanDistanceTo(Coordinate other)
	{
		double xDist = Math.abs(this.getX() - other.getX());
		double yDist = Math.abs(this.getY() - other.getY());
		return Math.sqrt(xDist * xDist + yDist * yDist);
	}

	public int chebyshevDistanceTo(Coordinate other)
	{
		int xDist = Math.abs(this.getX() - other.getX());
		int yDist = Math.abs(this.getY() - other.getY());

		return xDist > yDist ? xDist : yDist;
	}

}
