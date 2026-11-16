import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class RandProductSearch extends JFrame
{
    JPanel mainPnl;
    JPanel searchPnl;
    JPanel buttonPnl;

    JTextField searchTF;
    JTextArea resultsTA;
    JScrollPane scroller;
    TitledBorder resultsBorder;

    JButton searchBtn;
    JButton clearBtn;
    JButton quitBtn;

    Font labelFont = new Font("SansSerif", Font.PLAIN, 15);
    Font textFont = new Font("Monospaced", Font.PLAIN, 13);
    Font buttonFont = new Font("SansSerif", Font.BOLD, 16);

    public RandProductSearch()
    {
        mainPnl = new JPanel(new BorderLayout());

        createSearchPanel();
        mainPnl.add(searchPnl, BorderLayout.NORTH);

        resultsTA = new JTextArea();
        resultsTA.setFont(textFont);
        resultsTA.setEditable(false);
        scroller = new JScrollPane(resultsTA);
        resultsBorder = new TitledBorder("Results");
        scroller.setBorder(resultsBorder);
        mainPnl.add(scroller, BorderLayout.CENTER);

        createButtonPanel();
        mainPnl.add(buttonPnl, BorderLayout.SOUTH);

        add(mainPnl);
    }

    private void createSearchPanel()
    {
        searchPnl = new JPanel(new BorderLayout(10, 5));
        searchPnl.setBorder(new TitledBorder("Search " + RandProductMaker.DATA_FILE));

        JLabel searchLbl = new JLabel("Partial product name:");
        searchLbl.setFont(labelFont);

        searchTF = new JTextField();
        searchTF.setFont(labelFont);
        searchTF.addActionListener(e -> search());

        searchPnl.add(searchLbl, BorderLayout.WEST);
        searchPnl.add(searchTF, BorderLayout.CENTER);
    }

    private void createButtonPanel()
    {
        buttonPnl = new JPanel(new GridLayout(1, 3));

        searchBtn = new JButton("Search");
        clearBtn = new JButton("Clear");
        quitBtn = new JButton("Quit");

        for (JButton btn : new JButton[]{searchBtn, clearBtn, quitBtn})
        {
            btn.setFont(buttonFont);
            buttonPnl.add(btn);
        }

        searchBtn.addActionListener(e -> search());
        clearBtn.addActionListener(e ->
        {
            searchTF.setText("");
            resultsTA.setText("");
            resultsBorder.setTitle("Results");
            scroller.repaint();
            searchTF.requestFocus();
        });
        quitBtn.addActionListener(e -> System.exit(0));
    }

    private void search()
    {
        String term = searchTF.getText().trim();
        if (term.isEmpty())
        {
            JOptionPane.showMessageDialog(this, "Enter part of a product name to search for.");
            return;
        }

        File file = new File(RandProductMaker.DATA_FILE);
        if (!file.exists())
        {
            JOptionPane.showMessageDialog(this, "Data file " + file.getAbsolutePath()
                    + " not found.\nRun RandProductMaker first.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-6s  %-35s  %-40s  %10s%n", "ID", "Name", "Description", "Cost"));
        sb.append("-".repeat(97)).append(System.lineSeparator());

        int matches = 0;
        try (RandomAccessFile raf = new RandomAccessFile(file, "r"))
        {
            long records = raf.length() / Product.RECORD_SIZE;
            for (long i = 0; i < records; i++)
            {
                raf.seek(i * Product.RECORD_SIZE);
                Product p = Product.readRandomRecord(raf);
                if (p.getName().toLowerCase().contains(term.toLowerCase()))
                {
                    sb.append(String.format("%-6s  %-35s  %-40s  %10.2f%n",
                            p.getID(), p.getName(), p.getDescription(), p.getCost()));
                    matches++;
                }
            }
        }
        catch (IOException ex)
        {
            JOptionPane.showMessageDialog(this, "Error reading file: " + ex.getMessage());
            return;
        }

        if (matches == 0)
        {
            sb.append("No products match \"").append(term).append("\"");
        }

        resultsTA.setText(sb.toString());
        resultsTA.setCaretPosition(0);
        resultsBorder.setTitle("Results for \"" + term + "\" (" + matches + " match"
                + (matches == 1 ? "" : "es") + ")");
        scroller.repaint();
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

            RandProductSearch frame = new RandProductSearch();
            frame.setTitle("Random Access Product Search");
            frame.setSize(950, 500);
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        });
    }
}
