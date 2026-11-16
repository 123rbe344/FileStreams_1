import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class RandProductMaker extends JFrame
{
    public static final String DATA_FILE = "ProductData.dat";

    JPanel mainPnl;
    JPanel formPnl;
    JPanel buttonPnl;

    JTextField idTF;
    JTextField nameTF;
    JTextField descTF;
    JTextField costTF;
    JTextField countTF;

    JButton addBtn;
    JButton clearBtn;
    JButton quitBtn;

    int recordCount = 0;

    Font labelFont = new Font("SansSerif", Font.PLAIN, 15);
    Font buttonFont = new Font("SansSerif", Font.BOLD, 16);

    public RandProductMaker()
    {
        mainPnl = new JPanel(new BorderLayout());

        createFormPanel();
        mainPnl.add(formPnl, BorderLayout.CENTER);

        createButtonPanel();
        mainPnl.add(buttonPnl, BorderLayout.SOUTH);

        add(mainPnl);
        prepareDataFile();
    }

    private void createFormPanel()
    {
        formPnl = new JPanel(new GridBagLayout());
        formPnl.setBorder(new TitledBorder("Enter Product Data"));

        idTF = new JTextField(Product.ID_LENGTH + 4);
        nameTF = new JTextField(Product.NAME_LENGTH);
        descTF = new JTextField(Product.DESCRIPTION_LENGTH / 2);
        costTF = new JTextField(10);
        countTF = new JTextField(5);
        countTF.setEditable(false);
        countTF.setText("0");

        addRow(0, "ID (max " + Product.ID_LENGTH + "):", idTF);
        addRow(1, "Name (max " + Product.NAME_LENGTH + "):", nameTF);
        addRow(2, "Description (max " + Product.DESCRIPTION_LENGTH + "):", descTF);
        addRow(3, "Cost:", costTF);
        addRow(4, "Record count:", countTF);

        costTF.addActionListener(e -> addRecord());
    }

    private void addRow(int row, String labelText, JTextField field)
    {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.gridy = row;

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(labelFont);
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        formPnl.add(lbl, gbc);

        field.setFont(labelFont);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        formPnl.add(field, gbc);
    }

    private void createButtonPanel()
    {
        buttonPnl = new JPanel(new GridLayout(1, 3));

        addBtn = new JButton("Add Record");
        clearBtn = new JButton("Clear");
        quitBtn = new JButton("Quit");

        for (JButton btn : new JButton[]{addBtn, clearBtn, quitBtn})
        {
            btn.setFont(buttonFont);
            buttonPnl.add(btn);
        }

        addBtn.addActionListener(e -> addRecord());
        clearBtn.addActionListener(e -> clearFields());
        quitBtn.addActionListener(e -> System.exit(0));
    }

    /**
     * If a data file already exists, ask whether to start fresh or keep adding to it.
     */
    private void prepareDataFile()
    {
        File file = new File(DATA_FILE);
        if (!file.exists() || file.length() == 0)
        {
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this,
                DATA_FILE + " already exists with " + (file.length() / Product.RECORD_SIZE)
                        + " record(s).\nStart a new (empty) file?\n\nYes = overwrite, No = append",
                "Existing Data File", JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION)
        {
            if (!file.delete())
            {
                JOptionPane.showMessageDialog(this, "Could not delete the old data file.");
            }
        }
        else
        {
            recordCount = (int) (file.length() / Product.RECORD_SIZE);
            countTF.setText(String.valueOf(recordCount));
        }
    }

    private void addRecord()
    {
        String id = idTF.getText().trim();
        String name = nameTF.getText().trim();
        String desc = descTF.getText().trim();
        String costText = costTF.getText().trim();

        // Confirm every field was entered
        if (id.isEmpty() || name.isEmpty() || desc.isEmpty() || costText.isEmpty())
        {
            JOptionPane.showMessageDialog(this, "All fields are required.");
            return;
        }

        // Validate the field lengths
        if (id.length() > Product.ID_LENGTH)
        {
            JOptionPane.showMessageDialog(this, "ID must be " + Product.ID_LENGTH + " characters or fewer.");
            idTF.requestFocus();
            return;
        }
        if (name.length() > Product.NAME_LENGTH)
        {
            JOptionPane.showMessageDialog(this, "Name must be " + Product.NAME_LENGTH + " characters or fewer.");
            nameTF.requestFocus();
            return;
        }
        if (desc.length() > Product.DESCRIPTION_LENGTH)
        {
            JOptionPane.showMessageDialog(this, "Description must be " + Product.DESCRIPTION_LENGTH + " characters or fewer.");
            descTF.requestFocus();
            return;
        }

        // Validate the cost
        double cost;
        try
        {
            cost = Double.parseDouble(costText);
        }
        catch (NumberFormatException ex)
        {
            JOptionPane.showMessageDialog(this, "Cost must be a number.");
            costTF.requestFocus();
            return;
        }
        if (cost < 0)
        {
            JOptionPane.showMessageDialog(this, "Cost cannot be negative.");
            costTF.requestFocus();
            return;
        }

        Product product = new Product(id, name, desc, cost);

        try (RandomAccessFile raf = new RandomAccessFile(DATA_FILE, "rw"))
        {
            raf.seek(raf.length()); // append to the end of the file
            product.writeRandomRecord(raf);
        }
        catch (IOException ex)
        {
            JOptionPane.showMessageDialog(this, "Error writing to file: " + ex.getMessage());
            return;
        }

        recordCount++;
        countTF.setText(String.valueOf(recordCount));
        clearFields();
    }

    private void clearFields()
    {
        idTF.setText("");
        nameTF.setText("");
        descTF.setText("");
        costTF.setText("");
        idTF.requestFocus();
    }

    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(() ->
        {
            try
            {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }
            catch (Exception ex)
            {
            }

            RandProductMaker frame = new RandProductMaker();
            frame.setTitle("Random Access Product Maker");
            frame.setSize(800, 400);
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        });
    }
}
