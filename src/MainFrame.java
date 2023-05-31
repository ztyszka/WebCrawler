import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainFrame extends JFrame {
    static Font myFont = new Font("Arial", Font.BOLD, 16);
    JTextArea textArea = new JTextArea();
    File destinationFile;
    String settingsFilePath = "src/settings.txt";
    Document document;
    JTextField filterTextBox;
    String[] parseFilters = new String[]{"None", "Class name", "Id", "Type"};
    int selectedFilter;
    JTextField urlField;
    JComboBox<String> filterComboBox;
    JCheckBox chooseTextCheckbox;
    JCheckBox chooseAttributeCheckbox;
    JTextField chooseAttributeTextBox;
    JLabel saveToFileFileNameLabel;

    MainFrame() {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(800, 800);
        this.setTitle("Web Crawler");
        this.setLayout(null);

        // controls regarding reading url
        JLabel urlLabel = new JLabel("url:");
        urlLabel.setFont(myFont);
        urlLabel.setBounds(30, 25, 100, 40);
        this.add(urlLabel);

        urlField = new JTextField();
        urlField.setFont(myFont);
        urlField.setBounds(150, 25, 500, 40);
        this.add(urlField);

        JButton crawlButton = new JButton("Crawl");
        crawlButton.setFont(myFont);
        crawlButton.setBounds(670, 25, 100, 39);
        crawlButton.addActionListener(e -> {
            try {
                document = readWebPage(urlField.getText());
                textArea.setText(parseDocument().toString());
            } catch (IOException ex) {
                textArea.setText("Requested address cannot be reached");
                throw new RuntimeException(ex);
            }
        });
        this.add(crawlButton);
        this.getRootPane().setDefaultButton(crawlButton);

        // controls regarding filtering data
        JLabel filterByLabel = new JLabel("Filter by: ");
        filterByLabel.setFont(myFont);
        filterByLabel.setBounds(30, 75, 120, 40);
        this.add(filterByLabel);

        filterComboBox = new JComboBox<>(parseFilters);
        filterComboBox.setBounds(150, 75, 120, 40);
        filterComboBox.setFont(myFont);
        filterComboBox.addActionListener(e -> {
            selectedFilter = filterComboBox.getSelectedIndex();
            filterTextBox.setText("");
            filterTextBox.setVisible(selectedFilter != 0);
        });
        this.add(filterComboBox);

        filterTextBox = new JTextField();
        filterTextBox.setBounds(300, 75, 120, 40);
        filterTextBox.setFont(myFont);
        filterTextBox.setVisible(false);
        this.add(filterTextBox);

        JButton filterButton = new JButton("Filter");
        filterButton.setFont(myFont);
        filterButton.setBounds(650, 125, 120, 39);
        filterButton.addActionListener(e -> {
            try {
                textArea.setText(parseDocument().toString());
            } catch (Exception ex) {
                textArea.setText("There's no url selected!");
            }
        });
        this.add(filterButton);

        // controls regarding extracting data
        JLabel chooseLabel = new JLabel("Choose: ");
        chooseLabel.setFont(myFont);
        chooseLabel.setBounds(30, 125, 120, 40);
        this.add(chooseLabel);

        chooseTextCheckbox = new JCheckBox();
        chooseTextCheckbox.setText("Text");
        chooseTextCheckbox.setFont(myFont);
        chooseTextCheckbox.setBounds(150, 125, 100, 40);
        this.add(chooseTextCheckbox);

        chooseAttributeCheckbox = new JCheckBox();
        chooseAttributeCheckbox.setText("Attribute");
        chooseAttributeCheckbox.setFont(myFont);
        chooseAttributeCheckbox.setBounds(250, 125, 100, 40);
        chooseAttributeCheckbox.addActionListener(e -> chooseAttributeTextBox.setVisible(chooseAttributeCheckbox.isSelected()));
        this.add(chooseAttributeCheckbox);

        chooseAttributeTextBox = new JTextField();
        chooseAttributeTextBox.setBounds(400, 125, 120, 40);
        chooseAttributeTextBox.setFont(myFont);
        chooseAttributeTextBox.setVisible(false);
        this.add(chooseAttributeTextBox);

        JButton extractButton = new JButton("Extract");
        extractButton.setFont(myFont);
        extractButton.setBounds(650, 175, 120, 39);
        extractButton.addActionListener(e -> {
            try {
                if (destinationFile == null) {
                    JDialog errorDialog = new JDialog();
                    errorDialog.setLocationRelativeTo(this);
                    Container errorDialogContent = new JLabel("There's no file selected!");
                    errorDialog.setContentPane(errorDialogContent);
                    errorDialog.setSize(200, 100);
                    errorDialog.setVisible(true);
                } else {
                    saveSettingsToFile(
                            urlField.getText(),
                            Objects.requireNonNull(filterComboBox.getSelectedItem()).toString(),
                            filterTextBox.getText(),
                            chooseAttributeTextBox.getText()
                    );
                    extractToFile();
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
        this.add(extractButton);

        // checkbox regarding importing previous settings
        JCheckBox usePreviousSettingsCheckbox = new JCheckBox();
        usePreviousSettingsCheckbox.setText("Use previous settings");
        usePreviousSettingsCheckbox.setFont(myFont);
        usePreviousSettingsCheckbox.setBounds(30, 225, 250, 40);
        usePreviousSettingsCheckbox.addActionListener(e -> {
            try {
                if (usePreviousSettingsCheckbox.isSelected()){
                    getSettingsFromFile();
                    document = readWebPage(urlField.getText());
                    textArea.setText(parseDocument().toString());
                }

            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
        this.add(usePreviousSettingsCheckbox);

        // controls regarding saving data to file
        JLabel saveToFileLabel = new JLabel("Save to file: ");
        saveToFileLabel.setFont(myFont);
        saveToFileLabel.setBounds(30, 175, 100, 39);
        this.add(saveToFileLabel);

        saveToFileFileNameLabel = new JLabel("");
        saveToFileFileNameLabel.setBounds(300, 175, 120, 40);
        this.add(saveToFileFileNameLabel);

        JButton saveToFileButton = new JButton("Choose file");
        saveToFileButton.setFont(myFont);
        saveToFileButton.setBounds(150, 175, 120, 39);
        saveToFileButton.addActionListener(e -> {
            try {
                JFileChooser saveToFileChooser = new JFileChooser();
                saveToFileChooser.showSaveDialog(null);
                destinationFile = saveToFileChooser.getSelectedFile();
                saveToFileFileNameLabel.setText(destinationFile.getName());
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
        this.add(saveToFileButton);

        // area to show extracted data
        textArea.setFont(myFont);
        textArea.setEditable(false);
        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setBounds(20, 275, 750, 400);
        this.add(scroll);

        this.setVisible(true);
    }

    private void saveSettingsToFile(String url, String filter, String filterString, String attrText) throws IOException {
        try {
            FileWriter fileWriter = new FileWriter(settingsFilePath);
            PrintWriter printWriter = new PrintWriter(fileWriter);
            String isText = "";
            if (chooseTextCheckbox.isSelected()) isText = "y";
            String isAttr = "";
            if(chooseAttributeCheckbox.isSelected()) isAttr = "y";
            printWriter.printf("url %s \nfile %s \nfilter %s \nfilter_string %s \ntext %s \nattribute %s \nattr_name %s",
                    url, this.destinationFile.getAbsolutePath(), filter, filterString, isText, isAttr, attrText);
            printWriter.close();
            fileWriter.close();
        } catch (Exception e) {
            throw new IOException();
        }
    }

    private void getSettingsFromFile() throws IOException {
        try {
            var br = new BufferedReader(new FileReader(settingsFilePath));
            String line;
            while ((line = br.readLine()) != null) {
                String[] args = line.split(" ");
                switch (args[0]) {
                    case "url":
                        urlField.setText(args[1]);
                        break;
                    case "file":
                        StringBuilder filename = new StringBuilder(args[1]);
                        for (int i = 2; i < args.length; i++) {   // odtworzenie nazwy pliku zawierającej spacje
                            filename.append(" ");
                            filename.append(args[i]);
                        }
                        destinationFile = new File(filename.toString());
                        saveToFileFileNameLabel.setText(destinationFile.getName());
                        break;
                    case "filter":
                        filterComboBox.setSelectedIndex(new ArrayList<>(List.of(parseFilters)).indexOf(args[1]));
                        break;
                    case "filter_string":
                        if (args.length > 1)
                            filterTextBox.setText(args[1]);
                        break;
                    case "text":
                        if(args.length > 1)
                            chooseTextCheckbox.setSelected(true);
                        break;
                    case "attribute":
                        if(args.length > 1)
                            chooseAttributeCheckbox.setSelected(true);
                        break;
                    case "attr_name":
                        if(args.length > 1){
                            chooseAttributeTextBox.setVisible(true);
                            chooseAttributeTextBox.setText(args[1]);
                        }
                        break;
                    default:
                        break;
                }
            }
        } catch (Exception e) {
            throw new IOException();
        }
    }

    private Elements parseDocument() {
        switch (selectedFilter) {
            case 1:
                return Objects.requireNonNull(document.getElementsByClass(filterTextBox.getText()));
            case 2:
                return Objects.requireNonNull(document.getElementById(filterTextBox.getText())).children();
            case 3:  // Type
                return document.select(filterTextBox.getText());
            default: // None
                return document.children();
        }
    }

    public static Document readWebPage(String urltext) throws IOException {

        if (urltext.startsWith("http"))
            return Jsoup.connect(urltext).get();
        if (urltext.startsWith("www")) {
            urltext = "https://" + urltext;
            return Jsoup.connect(urltext).get();
        }
        if (urltext.startsWith("file:///")) {
            File input = new File(urltext.substring(8));
            return Jsoup.parse(input, "UTF-8");
        }
        File input = new File(urltext);
        return Jsoup.parse(input, "UTF-8");
    }

    public void extractToFile() {
        try {
            FileWriter fileWriter = new FileWriter(destinationFile, true);
            PrintWriter printWriter = new PrintWriter(fileWriter);

            Elements elements = parseDocument();
            for (Element element : elements) {
                if (chooseTextCheckbox.isSelected())
                    printWriter.printf("%s\n", element.text());
                if (chooseAttributeCheckbox.isSelected()) {
                    String attributeName = chooseAttributeTextBox.getText();
                    if(element.attributes().hasDeclaredValueForKey(attributeName))
                        printWriter.printf("%s = %s\n", attributeName, element.attr(attributeName));
                }
            }
            printWriter.close();
            fileWriter.close();
        } catch (Exception ex) {
            textArea.setText("There's no file selected!");
        }
    }
}
