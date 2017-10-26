package net.amarantha.gpiomofo;
/**
 * DMX Monitoring widget
 * for use with the com.s5g.util.Dmx package
 * released into the public domain by
 * David K Eggen
 */

import javax.swing.*;
import java.awt.*;

public class Monitor extends JFrame {
	
  JLabel     labs[];
  GridLayout G;
  int        sz   = 250;
  int        cols = 13;
  public boolean    debug= false;
  
  public static void main(String argv[]){
	System.out.println("Monitor Demo");
	Monitor M = new Monitor();
	M.setVisible(true);
	M.setChan(75,18);
	M.setChan(14,200);
    }
	  
  
  public Monitor(){
	setTitle("DMX Monitoring");
    setSize(500,400);
	//System.out.println("Cols:"+cols);
	labs = new JLabel[sz];  
	G    = new GridLayout(9,6);
	System.out.println("cols:"+G.getColumns());
	setLayout(G);
	labs = new JLabel[sz];
	add(new JLabel("Chanels"));
	add(new JLabel("00")); add(new JLabel("01")); add(new JLabel("02"));
	add(new JLabel("03")); add(new JLabel("04")); add(new JLabel("05")); 
	add(new JLabel("06")); add(new JLabel("07")); add(new JLabel("08")); 
	add(new JLabel("09"));
	
	int i;
	int j;
	int k =0;
	for(i =0;i<8;i++){
	  add(new JLabel(i+""));
	  for(j=0;j<10;j++){
		labs[k] = new JLabel(0+"");
		labs[k].setText("--");
		add(labs[k]);
		k++;
	    }
	  }

    }
  public boolean setChan(int chan, int val){
	if(debug){System.out.println("Monitor.setChan("+chan+","+val+")"); }
	if(chan<0){   return false; }
	if(chan >99){ return false; }
	if(val < 0) { return false; }
	if(val >255){ return false; }
	labs[chan].setText(""+val);
    return true;
    }
  }
