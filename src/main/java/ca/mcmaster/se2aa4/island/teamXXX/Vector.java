package ca.mcmaster.se2aa4.island.teamXXX;

import java.util.Objects;

public class Vector {
    public Integer x;
    public Integer y;

    public Vector(Integer x, Integer y) {
        this.x = x;
        this.y = y;
    }

    public Vector add(Vector vector) {
        return new Vector(this.x + vector.x, this.y + vector.y);
    }

    public Vector subtract(Vector vector) {
        return new Vector(this.x - vector.x, this.y - vector.y);
    }

    public Vector multiply(Integer scalar) {
        return new Vector(this.x * scalar, this.y * scalar);
    } 

    public Vector copy() {
        return new Vector(this.x, this.y);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Vector vector = (Vector) obj;
        return Objects.equals(x, vector.x) && Objects.equals(y, vector.y);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    public String toString() {
        return "(" + this.x + ", " + this.y + ")";
    }
}
