package ua.com.tehavto;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class View extends JFrame {

    String logist, warehouse, city, region, clientName, tel;
    ArrayList<Good> goods;

    private JLabel enterOrderNumber;
    private JLabel nalozhkaLabel;
    private JLabel summaNalozhkiLabel;
    private JLabel skladLabel;
    private JComboBox comboBox;
    private boolean nalozhka = false;

    private JTextField orderNumber;
    private JTextField summaNalozhki;
    private JTextField sklad;
    private JButton enterButton;
    private JLabel uri;
    private JLabel clientLabel;

    HSSFWorkbook workbook;
    HSSFSheet sheet;


    public View() {

        initUI();
        addListeners();

    }


    /**
     * Initialization of all components of UI
     */
    private void initUI(){
        setTitle("Zakaz");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(500, 300);
        setLocation(300, 70);
        setLayout(new GridLayout(0, 1));
        enterOrderNumber = new JLabel("Номер заказа");
        orderNumber = new JTextField(5);
        // orderNumber.setText("1001304");  //hardcode for testing
        enterButton = new JButton("Получить файл");
        enterButton.setSize(100, 100);
        uri = new JLabel();
        clientLabel = new JLabel();
        nalozhkaLabel = new JLabel("Наложка: ");
        summaNalozhkiLabel = new JLabel("Сумма");
        skladLabel = new JLabel("Склад");
        summaNalozhki = new JTextField(5);
        sklad = new JTextField(3);
        summaNalozhki.setEditable(false);
        comboBox = new JComboBox();
        comboBox.addItem("Нет");
        comboBox.addItem("да");
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());
        panel.add(enterOrderNumber);
        panel.add(orderNumber);
        JPanel panel2 = new JPanel();
        panel2.setLayout(new FlowLayout());
        panel2.add(skladLabel);
        panel2.add(sklad);
        panel2.add(nalozhkaLabel);
        panel2.add(comboBox);
        panel2.add(summaNalozhkiLabel);
        panel2.add(summaNalozhki);
        this.add(panel);
        this.add(panel2);
        this.add(enterButton);
        this.add(uri);
        this.add(clientLabel);
        uri.setVisible(false);
        clientLabel.setVisible(false);

        setVisible(true);
    }

    /**
     * Adding listeners for components
     */
    private void addListeners(){
        comboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(comboBox.getSelectedIndex()==0){
                    summaNalozhki.setEditable(false);
                    summaNalozhki.setText("");
                    nalozhka = false;
                }
                if(comboBox.getSelectedIndex()==1){
                    summaNalozhki.setEditable(true);
                    nalozhka = true;
                }
            }
        });

        enterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (orderNumber.getText().isEmpty() || orderNumber.getText().length() < 4 || !isOnlyNumbers(orderNumber.getText()) || !isValidStart(orderNumber.getText())) {
                    JOptionPane.showMessageDialog(null, "Нужно ввести номер заказа \nначиная со 100(напр. 1001307)");
                } else {
                    try {
                        parsing(orderNumber.getText());
                        uri.setText("Файл Order" + orderNumber.getText() + "_teh-avto.xls в корне диска F");
                        clientLabel.setText("Клиент - " + clientName + ", " + city);
                        uri.setVisible(true);
                        clientLabel.setVisible(true);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                        JOptionPane.showMessageDialog(null, "Что-то пошло не так\nможет нет инета");
                        uri.setVisible(false);
                        clientLabel.setVisible(false);
                    } catch (NoOrderException e1) {
                        JOptionPane.showMessageDialog(null, "Нет такого номера заказа");
                    }

                }
            }
        });
    }


    /**
     *
     * @param order
     * @throws IOException
     * @throws NoOrderException
     */
    private void parsing(String order) throws IOException, NoOrderException {
        String actualNumber = order.substring(3);
        System.out.println(actualNumber);

        Document document = Jsoup.connect("https://teh-avto.com.ua/webasyst/shop/?module=order&id=%20" + actualNumber + "&state_id=new|processing|paid&_=1523997971688") // логинимся, ссылка взята из атрибута action формы
                .userAgent("Mozilla")
                .data("wa_auth_login", "1")
                .data("login", "admin")
                .data("password", "111") // enter correct password
                .data("remember", "1")
                .data("_csrf", "5ad5c6c27822c1.49459053")
                .post();

        if (document.outerHtml().contains("В этом списке нет заказов")) {
            throw new NoOrderException();
        }

        Elements h3Elements = document.select("h3");
        Elements pElements = document.select("p");
        Elements liElements = document.select("li");
        Elements trElements = document.select("tr");


        clientName = h3Elements.get(0).ownText();
        logist = h3Elements.get(1).child(1).ownText();
        String text = pElements.first().html();
        if (text.contains("br")) {
            String[] textSplitResult = text.split("<br>");
            if (textSplitResult[0].charAt(0) == '<') {
                city = textSplitResult[0].substring(textSplitResult[0].lastIndexOf('>') + 1);
            } else city = textSplitResult[0];
            region = textSplitResult[1].trim();
        } else {
            city = text;
            region = "";
        }
        for (Element e : liElements) {
            if (!e.ownText().isEmpty() && e.ownText().charAt(0) == '(') {
                tel = e.ownText();
            }
        }

        goods = new ArrayList<>();

        for (int i = 1; i < trElements.size() - 5; i++) {
            String name = trElements.get(i).child(1).child(0).ownText();
            String art = "";
            if (trElements.get(i).child(1).html().contains("<br>")) {
                art = trElements.get(i).child(1).child(2).ownText();
            }
            int quant = Integer.parseInt(trElements.get(i).child(2).ownText());
            goods.add(new Good(art, name, quant));
        }

        for (Good g : goods) {
            System.out.println(g.toString());
        }

        workbook = new HSSFWorkbook();
        sheet = workbook.createSheet("list");

        CellRangeAddress region1 = new CellRangeAddress(1, 1, 1, 2);
        sheet.addMergedRegion(region1);
        CellRangeAddress region2 = new CellRangeAddress(2, 2, 1, 2);
        sheet.addMergedRegion(region2);
        CellRangeAddress region3 = new CellRangeAddress(3, 3, 1, 2);
        sheet.addMergedRegion(region3);

        int rowNumber = 0;

        Row row0 = sheet.createRow(rowNumber++);
        Row row1 = sheet.createRow(rowNumber++);
        Row row2 = sheet.createRow(rowNumber++);
        Row row3 = sheet.createRow(rowNumber++);
        Row row4 = sheet.createRow(rowNumber++);
        Row row5 = sheet.createRow(rowNumber++);


        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setFillForegroundColor((short) 150);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);

        CellStyle style2 = workbook.createCellStyle();
        style2.setAlignment(HorizontalAlignment.CENTER);
        style2.setBorderTop(BorderStyle.THIN);
        style2.setBorderRight(BorderStyle.THIN);
        style2.setBorderBottom(BorderStyle.THIN);
        style2.setBorderLeft(BorderStyle.THIN);

        Cell a1 = row1.createCell(1);
        a1.setCellValue("Перевозчик");
        a1.setCellStyle(style);
        Cell a11 = row1.createCell(2);
        a11.setCellStyle(style);
        Cell a2 = row1.createCell(3);
        a2.setCellValue(logist);
        a2.setCellStyle(style2);

        Cell b1 = row2.createCell(1);
        b1.setCellValue("Наложенный платеж");
        b1.setCellStyle(style);
        Cell b11 = row2.createCell(2);
        b11.setCellStyle(style);
        Cell b2 = row2.createCell(3);
        if(nalozhka)
        b2.setCellValue(summaNalozhki.getText()+" грн");
        else b2.setCellValue("нет");
        b2.setCellStyle(style2);

        Cell c1 = row3.createCell(1);
        c1.setCellValue("Город, склад, получатель");
        c1.setCellStyle(style);
        Cell c11 = row3.createCell(2);
        c11.setCellStyle(style);
        Cell c2 = row3.createCell(3);
        c2.setCellValue(city + "-"+sklad.getText()+", " + region + " обл., " + clientName + ", т.:" + tel);
        c2.setCellStyle(style2);

        Cell d1 = row5.createCell(1);
        d1.setCellValue("№");
        d1.setCellStyle(style);
        Cell d2 = row5.createCell(2);
        d2.setCellValue("артикул");
        d2.setCellStyle(style);
        Cell d3 = row5.createCell(3);
        d3.setCellValue("Наименование");
        d3.setCellStyle(style);
        Cell d4 = row5.createCell(4);
        d4.setCellValue("Количество");
        d4.setCellStyle(style);

        Row r;
        for (int i = 0; i < goods.size(); i++) {
            r = sheet.createRow(rowNumber++);
            Cell r1 = r.createCell(1);
            r1.setCellValue(i + 1);
            r1.setCellStyle(style2);
            Cell r2 = r.createCell(2);
            r2.setCellValue(goods.get(i).getArticul());
            r2.setCellStyle(style2);
            Cell r3 = r.createCell(3);
            r3.setCellValue(goods.get(i).getName());
            r3.setCellStyle(style2);
            Cell r4 = r.createCell(4);
            r4.setCellValue(goods.get(i).getQuant());
            r4.setCellStyle(style2);
        }

        sheet.autoSizeColumn(3, true);
        sheet.autoSizeColumn(4, true);
        sheet.setColumnWidth(1, 1000);
        sheet.setColumnWidth(2, 5000);


      //  try (FileOutputStream out = new FileOutputStream(new File("F:\\Order100" + actualNumber + "_teh-avto.xls"))) {  // for testing
        try (FileOutputStream out = new FileOutputStream(new File("C:\\Users\\User\\Desktop\\Бланк заказа\\Order100" + actualNumber + "_teh-avto.xls"))) {  //for client
            workbook.write(out);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Ошибка записи файла" +
                    "\nвозможно этот файл открыт в Excel\nзакройте и попробуйте еще раз");
        }
    }

    /**
     * Checking is there only digits in string "text"
     * @param text
     * @return  true/false
     */
    private boolean isOnlyNumbers(String text) {
        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] < 48 || chars[i] > 58)
                return false;
        }
        return true;
    }

    /**
     * Order number must have been started with "100". method checks it
     * @param orderNumber
     * @return true/false
     */
    private boolean isValidStart(String orderNumber) {
        char[] chars = orderNumber.toCharArray();
        if (chars[0] != '1') return false;
        if (chars[1] != '0') return false;
        if (chars[2] != '0') return false;
        return true;
    }

}
