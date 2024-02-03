import ithakimodem.Modem;
import java.io.FileOutputStream;
import java.io.IOException;


public class UserApplication {
    public static void main(String[] param) throws Exception {
        (new UserApplication()).demo();
    }

    public void read(Modem modem) {
        for (;;) {
            try {
                int k;
                k=modem.read();
                if (k==-1) break;
                System.out.print((char)k);
            } catch (Exception x) {
                break;
            }
        }
    }

    public void demo() {
        
        Modem modem;
        modem=new Modem();
        modem.setSpeed(80000);
        modem.setTimeout(2000);

        modem.open("ithaki");
        read(modem);

        //String s="echo_request\r";
        //echo(modem, s);
        
        //String b="image_request_errorfree\r";
        //modem.write(b.getBytes());
        //image(modem,"imagewithoutnoise.jpg");

        //String b="image_request_error\r";
        //modem.write(b.getBytes());
        //image(modem,"imagewithnoise.jpg");

        //String c="gps_request";
        //gpsimage(modem, c);
        
        //String a="ack\r";
        //String n="nack\r";
        //arq(modem,n,a);
    }



public void arq(Modem modem, String nack, String ack) {
    try (FileOutputStream echofile=new FileOutputStream("arq.txt")) {
            long elapsedtime=0;
            long starttime=System.currentTimeMillis();
            String s=ack;
            long start=0;
            int resents=0;
            while (elapsedtime<300000) {
            modem.write(s.getBytes());
            if (s==ack) {resents=0; start=System.currentTimeMillis();}
            long stop=0;
            String packet="";
                    for (;;) {
            try {
                int k;
                k=modem.read();
                packet=packet+(char)k;
                if (packet.contains("PSTOP")) {
                    String message=packet.substring(packet.length()-11-16, packet.length()-11);
                    int fcs=Integer.parseInt(packet.substring(packet.length()-8, packet.length()-6));
                    int xor=message.charAt(0);
                    for (int i=1;i<message.length();i++) {
                        xor=xor^message.charAt(i);
                    }
                    if (xor==fcs) {
                        stop=System.currentTimeMillis();
                        long time=stop-start;
                        echofile.write((String.valueOf(elapsedtime)+"\t"+String.valueOf(time)+"\t"+String.valueOf(resents)+"\n").getBytes());
                        s=ack;
                        break;
                    } else {
                        resents++;
                        s=nack;
                        break;
                    }
                    
                }
                if (k==-1) {
                    break;
                }
            }
             catch (Exception x) {
                break;
            }
        }
        elapsedtime=System.currentTimeMillis()-starttime;
    }
 } catch (IOException e) {
            e.printStackTrace();
        }
}


public void echo(Modem modem, String s) {
            try (FileOutputStream echofile=new FileOutputStream("echotime.txt")) {
            long elapsedtime=0;
            long starttime=System.currentTimeMillis();
            while (elapsedtime<300000) {
            modem.write(s.getBytes());
            long start=System.currentTimeMillis();
            long stop=0;
            String packet="";
                    for (;;) {
            try {
                int k;
                k=modem.read();
                packet=packet+(char)k;
                if (packet.contains("PSTOP")) {stop=System.currentTimeMillis(); 
                    long time=stop-start;
            echofile.write((String.valueOf(elapsedtime)+"\t"+String.valueOf(time)+"\n").getBytes());
             break;}
                if (k==-1) {
                    break;}
            
            } catch (Exception x) {
                break;
            }
        }
        elapsedtime=System.currentTimeMillis()-starttime;
    }
 } catch (IOException e) {
            e.printStackTrace();
        }
}

public void gpsimage(Modem modem,String c) {
            //R=1010099 First Session
            //R=1001199 Second Session
            String R="R=1001199\r";
                modem.write((c+R).getBytes());
                String result;
                StringBuilder sb=new StringBuilder();
                 for (;;) {
            try {
                int k;
                k=modem.read();
                if (k==-1) break;
                sb.append((char)k);
            } catch (Exception x) {
                break;
            }
        }
            result=sb.toString();
            String resultnew=result.substring(27, result.length()-1-27);
            String parts[]=resultnew.split("\n");
            int traces=9;
            String geowidth[]=new String[99];
            String geolength[]=new String[99];
            for (int i=0;i<99;i++) {
                char x='0'; char y='0';
                int j=0;
                while(x!='N') {
                    j++;
                    x=parts[i].charAt(j);
                }
                geowidth[i]=parts[i].substring(j-9-1, j-1);
                j=0;
                while (y!='E') {
                    j++;
                    y=parts[i].charAt(j);
                }
                geolength[i]=parts[i].substring(j-9-1, j-1);
            }
            int tracesm[]=new int[traces];
                int sec=0;
                int k=0;
                for (int i=0;i<traces;i++) {
                    for(;k<99;k++) {
                        String secpart=parts[k].substring(11,13);
                        if (Integer.parseInt(secpart)==sec) {
                            tracesm[i]=k;
                            break;
                        }
                    }
                    sec=sec+4;
                }
                String t="";
                for (int i=0;i<traces;i++) {
                    int degwidth=Integer.parseInt(geowidth[tracesm[i]].substring(0, 2)); String s4=Integer.toString(degwidth);
                    int firstminwidth=Integer.parseInt(geowidth[tracesm[i]].substring(2,4)); String s5=Integer.toString(firstminwidth);
                    int secondminwidth=(int)(60*Integer.parseInt(geowidth[tracesm[i]].substring(5,8))/1000); String s6=Integer.toString(secondminwidth);
                    int deglength=Integer.parseInt(geolength[tracesm[i]].substring(0, 2)); String s1=Integer.toString(deglength);
                    int firstminlength=Integer.parseInt(geolength[tracesm[i]].substring(2,4)); String s2=Integer.toString(firstminlength);
                    int secondminlength=(int)(60*Integer.parseInt(geolength[tracesm[i]].substring(5,8))/1000);String s3=Integer.toString(secondminlength);
                    t=t+"T="+s1+s2+s3+s4+s5+s6;
                }
                t=t+"\r";
        modem.write((c+t).getBytes());
        image(modem,"gps.jpg");
}
    



public void image(Modem modem,String name) {
        try{
        try (FileOutputStream imagedata = new FileOutputStream(name)) {
            for (;;) {
                try {
                    int k;
                    k=modem.read();
                    if (k==-1) break;
                    imagedata.write((byte)k);
                } catch (Exception x) {
                    break;
                }
            }
            imagedata.close();
        }
		} 
		 catch (IOException e) {
			e.printStackTrace();
		}
}
}