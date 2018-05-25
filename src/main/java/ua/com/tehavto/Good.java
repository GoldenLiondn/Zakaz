package ua.com.tehavto;

import java.util.Objects;

public class Good {
    private String articul;
    private String name;
    private int quant;

    public Good(String articul, String name, int quant) {
        this.articul = articul;
        this.name = name;
        this.quant = quant;
    }

    public String getArticul() {
        return articul;
    }

    public String getName() {
        return name;
    }

    public int getQuant() {
        return quant;
    }

    @Override
    public String
    toString() {
        return "Good{" +
                "articul='" + articul + '\'' +
                ", name='" + name + '\'' +
                ", quant='" + quant + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Good good = (Good) o;
        return quant == good.quant &&
                Objects.equals(articul, good.articul) &&
                Objects.equals(name, good.name);
    }

    @Override
    public int hashCode() {

        return Objects.hash(articul, name, quant);
    }
}
