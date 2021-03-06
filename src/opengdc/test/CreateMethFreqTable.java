package opengdc.test;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import opengdc.util.GDCData;

/**
 *
 * @author fabio
 */
public class CreateMethFreqTable {
    
    private static final String ROOT = "/galaxy/home/fabio/gdc-meth/meth/";
    
    public static void main(String[] args) {
        ArrayList<String> diseases = new ArrayList<>(GDCData.getBigGDCDataMap().get("TCGA").keySet());
        File data_folder = new File(ROOT);
        if (data_folder.exists()) {
            for (String disease: diseases) {
                ArrayList<String> platforms = new ArrayList<>();
                platforms.add("HumanMethylation27");
                platforms.add("HumanMethylation450");
                for (String platform: platforms) {
                    System.err.println("processing "+disease+" - platform: "+platform);
                    String disease_abbr = disease.toUpperCase().split("-")[1];
                    try {
                        System.err.println("retrieving sites");
                        ArrayList<String> sites = getSites(data_folder, disease_abbr, platform);
                        System.err.println("retrieving aliquots");
                        ArrayList<String> aliquots = getAliquots(data_folder, disease_abbr, platform);
                        System.err.println("retrieving beta values");
                        double[][] beta_values = getBetaValues(data_folder, sites.size(), aliquots, platform);
                        if (beta_values != null) {
                            String matrixFilePath = ROOT+disease+"_"+platform+"_beta_matrix.tsv";
                            System.err.println("printing matrix");
                            printBetaValues(matrixFilePath, sites, aliquots, beta_values);
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            System.err.println("--------------");
        }
    }
    
    private static ArrayList<String> getSites(File data_folder, String disease, String platform) {
        ArrayList<String> sites = new ArrayList<>();
        for (File f: data_folder.listFiles()) {
            if (f.getName().toLowerCase().endsWith("txt") && f.getName().toUpperCase().contains("_"+disease+"."+platform.toUpperCase())) {
                try {
                    InputStream fstream = new FileInputStream(f.getAbsolutePath());
                    DataInputStream in = new DataInputStream(fstream);
                    BufferedReader br = new BufferedReader(new InputStreamReader(in));
                    String line;
                    boolean skipHeader = true;
                    while ((line = br.readLine()) != null) {
                        if (!skipHeader) {
                            if (!line.trim().equals("")) {
                                String[] line_split = line.split("\t");
                                String site = line_split[0];
                                sites.add(site);
                            }
                        }
                        skipHeader = false;
                    }
                    br.close();
                    in.close();
                    fstream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
        }
        return sites;
    }

    private static ArrayList<String> getAliquots(File data_folder, String disease, String platform) {
        ArrayList<String> aliquots = new ArrayList<>();
        for (File f: data_folder.listFiles()) {
            if (f.getName().toLowerCase().endsWith("txt") && f.getName().toUpperCase().contains("_"+disease+"."+platform.toUpperCase())) {
                String[] f_name_split = f.getName().split("\\.");
                String aliquot = f_name_split[5];
                aliquots.add(aliquot);
            }
        }
        return aliquots;
    }
    
    private static double[][] getBetaValues(File data_folder, int sites, ArrayList<String> aliquots, String platform) throws Exception {
        double[][] beta_values = new double[sites][aliquots.size()];
        for (int i=0; i<aliquots.size(); i++) {
            String aliquot = aliquots.get(i);
            String file_path = "";
            for (String fileName: data_folder.list()) {
                if (fileName.toLowerCase().contains(aliquot.toLowerCase()) && fileName.toLowerCase().contains(platform.toLowerCase()))
                    file_path = data_folder.getAbsolutePath()+"/"+fileName;
            }
            if ((new File(file_path)).exists()) {
                try {
                    InputStream fstream = new FileInputStream(file_path);
                    DataInputStream in = new DataInputStream(fstream);
                    BufferedReader br = new BufferedReader(new InputStreamReader(in));
                    String line;
                    int line_count = 0;
                    boolean skipHeader = true;
                    while ((line = br.readLine()) != null) {
                        if (!skipHeader) {
                            if (!line.trim().equals("")) {
                                String[] line_split = line.split("\t");
                                String site = line_split[0];
                                String beta_value_str = line_split[1];
                                double beta_value = Double.NaN;
                                if (!beta_value_str.toLowerCase().trim().equals("null") && !beta_value_str.toLowerCase().trim().equals("") && !beta_value_str.toLowerCase().trim().equals("na"))
                                    beta_value = Double.valueOf(beta_value_str);
                                int row = line_count;
                                int column = i;
                                beta_values[row][column] = beta_value;
                                line_count++;
                            }
                        }
                        skipHeader = false;
                    }
                    br.close();
                    in.close();
                    fstream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else throw new Exception("");
        }
        return beta_values;
    }

    private static void printBetaValues(String matrixFilePath, ArrayList<String> sites, ArrayList<String> aliquots, double[][] beta_values) {
        try {
            FileOutputStream fos = new FileOutputStream(matrixFilePath);
            PrintStream out = new PrintStream(fos);
            //print header
            //String line = "\t\t";
            String line = "\t";
            for (int i=0; i<aliquots.size(); i++)
                line = line + aliquots.get(i) + "\t";
            line = line.substring(0, line.length()-1);
            out.println(line);
            
            //print data
            for (int i=0; i<sites.size(); i++) {
                String site = sites.get(i);
                //System.err.println("> "+site);
                /*String gene = "";
                if (site2gene.containsKey(site))
                    gene = site2gene.get(site);*/
                //line = site + "\t" + gene + "\t";
                line = site + "\t";
                for (int j=0; j<beta_values[i].length; j++)
                    line = line + String.valueOf(beta_values[i][j]) + "\t";
                line = line.substring(0, line.length()-1);
                out.println(line);
            }
            
            out.close();
            fos.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
