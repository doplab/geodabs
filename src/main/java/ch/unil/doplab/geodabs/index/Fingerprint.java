package ch.unil.doplab.geodabs.index;

public class Fingerprint implements Comparable<Fingerprint> {

    public final int hash;
    public final int position;

    public Fingerprint(int hash, int position) {
        this.hash = hash;
        this.position = position;
    }

    @Override
    public int compareTo(Fingerprint o) {
        return Integer.compare(position, o.position);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Fingerprint that = (Fingerprint) o;
        return hash == that.hash;
    }

    @Override
    public int hashCode() {
        return (int) hash;
    }

    @Override
    public String toString() {
        return String.valueOf(this.hash);
    }
}
