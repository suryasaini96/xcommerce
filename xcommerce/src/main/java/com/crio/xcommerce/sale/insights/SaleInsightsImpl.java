package com.crio.xcommerce.sale.insights;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.crio.xcommerce.contract.exceptions.AnalyticsException;
import com.crio.xcommerce.contract.insights.SaleAggregate;
import com.crio.xcommerce.contract.insights.SaleAggregateByMonth;
import com.crio.xcommerce.contract.insights.SaleInsights;
import com.crio.xcommerce.contract.resolver.DataProvider;

public class SaleInsightsImpl implements SaleInsights {

    private double totalSales = 0;
    private double[][] monthWiseSales = new double[13][1];

    @Override
    public SaleAggregate getSaleInsights(DataProvider dataProvider, int year) throws IOException, AnalyticsException {

        File csvFile = dataProvider.resolveFile();
        String vendorName = dataProvider.getProvider();
        SaleAggregate saleAggregate =  new SaleAggregate();
        List<SaleAggregateByMonth> aggregateByMonths = new ArrayList<SaleAggregateByMonth>();
        String line;
        double amount;
        LocalDate date;
        //BufferedReader br = new BufferedReader(new FileReader(csvFile));
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(csvFile), "UTF-8"));
        try {
            String s = br.readLine(); // ignore the first line of file
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (vendorName == "flipkart"){
            //transaction_id,external_transaction_id,user_id, transaction_date,transaction_status,amount
            while ((line=br.readLine())!=null) {
                try{
                    amount = Double.parseDouble(line.split(",")[5]);
                    date = LocalDate.parse(line.split(",")[3]);
                } catch (NumberFormatException | DateTimeParseException e){
                    br.close();
                    throw new AnalyticsException("Mandatory data is missing!");
                } 
                String status = line.split(",", -1)[4];
                List<String> validStatus = Arrays.asList("complete", "paid", "shipped");
                calculateSales(validStatus, status, year, date, amount);
            }
        }

        else if (vendorName == "amazon"){
            //transaction_id,ext_txn_id,user_id,status,date,amount
            while ((line=br.readLine())!=null) {
                try{
                    amount = Double.parseDouble(line.split(",", -1)[5]);
                    date = LocalDate.parse(line.split(",", -1)[4]);
                } catch (NumberFormatException | DateTimeParseException e){
                    br.close();
                    throw new AnalyticsException("Mandatory data is missing!");
                } 
                String status = line.split(",", -1)[3];
                List<String> validStatus = Arrays.asList("shipped");
                calculateSales(validStatus, status, year, date, amount);
            }
        }

        else if (vendorName == "ebay"){
            //txn_id,username,transaction_status,transaction_date,amount
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            while ((line=br.readLine())!=null) {
                try{
                    amount = Double.parseDouble(line.split(",", -1)[4]);
                    date = LocalDate.parse(line.split(",", -1)[3], dtf);
                } catch (NumberFormatException | DateTimeParseException e){
                    br.close();
                    throw new AnalyticsException("Mandatory data is missing!");
                } 
                String status = line.split(",", -1)[2];
                List<String> validStatus = Arrays.asList("complete", "Delivered");
                calculateSales(validStatus, status, year, date, amount);
            }  
        }
        br.close(); 
        for (int i=1; i<=12; i++){
            double sales = monthWiseSales[i][0];
            aggregateByMonths.add(new SaleAggregateByMonth(i, sales));
        }
        saleAggregate.setTotalSales(totalSales);
        saleAggregate.setAggregateByMonths(aggregateByMonths);
        return saleAggregate;
    }

    private void calculateSales(List<String> validStatus, String status, int year, LocalDate date, double amount) {
        if (date.getYear() == year && validStatus.contains(status)) {
            int m = date.getMonthValue();
            monthWiseSales[m][0]+= amount;
            totalSales+= amount;
        }
    }
}