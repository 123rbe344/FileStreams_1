import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.Objects;

public class Product implements Serializable
{
    private static final long serialVersionUID = 1L;

    // Fixed field lengths (in characters) for the Random Access file
    public static final int ID_LENGTH = 6;
    public static final int NAME_LENGTH = 35;
    public static final int DESCRIPTION_LENGTH = 75;

    // writeChars() uses 2 bytes per char, writeDouble() uses 8 bytes
    public static final int RECORD_SIZE = (ID_LENGTH + NAME_LENGTH + DESCRIPTION_LENGTH) * 2 + 8;

    private final String ID; // should never change
    private String name;
    private String description;
    private double cost;

    public Product(String ID, String name, String description, double cost)
    {
        this.ID = ID.trim();
        this.name = name.trim();
        this.description = description.trim();
        this.cost = cost;
    }

    public String getID()
    {
        return ID;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name.trim();
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description.trim();
    }

    public double getCost()
    {
        return cost;
    }

    public void setCost(double cost)
    {
        this.cost = cost;
    }

    // ---------- Random Access utility methods ----------

    /**
     * Pads (or truncates) a String with trailing spaces so it is exactly length characters.
     */
    public static String pad(String value, int length)
    {
        if (value.length() >= length)
        {
            return value.substring(0, length);
        }
        StringBuilder sb = new StringBuilder(value);
        while (sb.length() < length)
        {
            sb.append(' ');
        }
        return sb.toString();
    }

    public String getPaddedID()
    {
        return pad(ID, ID_LENGTH);
    }

    public String getPaddedName()
    {
        return pad(name, NAME_LENGTH);
    }

    public String getPaddedDescription()
    {
        return pad(description, DESCRIPTION_LENGTH);
    }

    /**
     * Returns the full fixed-length record (all String fields padded) as one String.
     */
    public String toRandomRecord()
    {
        return getPaddedID() + getPaddedName() + getPaddedDescription() + cost;
    }

    /**
     * Writes this product as one fixed-size record at the file's current position.
     */
    public void writeRandomRecord(RandomAccessFile raf) throws IOException
    {
        raf.writeChars(getPaddedID());
        raf.writeChars(getPaddedName());
        raf.writeChars(getPaddedDescription());
        raf.writeDouble(cost);
    }

    /**
     * Reads one fixed-size record from the file's current position.
     */
    public static Product readRandomRecord(RandomAccessFile raf) throws IOException
    {
        String id = readFixedString(raf, ID_LENGTH);
        String name = readFixedString(raf, NAME_LENGTH);
        String description = readFixedString(raf, DESCRIPTION_LENGTH);
        double cost = raf.readDouble();
        return new Product(id, name, description, cost);
    }

    private static String readFixedString(RandomAccessFile raf, int length) throws IOException
    {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++)
        {
            sb.append(raf.readChar());
        }
        return sb.toString().trim();
    }

    // ---------- Other formats ----------

    public String toCSV()
    {
        return ID + ", " + name + ", " + description + ", " + cost;
    }

    @Override
    public String toString()
    {
        return "Product{ID='" + ID + "', name='" + name + "', description='" + description + "', cost=" + cost + "}";
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof Product)) return false;
        Product p = (Product) o;
        return Double.compare(p.cost, cost) == 0
                && ID.equals(p.ID)
                && name.equals(p.name)
                && description.equals(p.description);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(ID, name, description, cost);
    }
}
