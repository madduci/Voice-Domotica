/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package voicedomotica;


import java.io.InputStream;
import java.io.OutputStream;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import java.util.Enumeration;

public class Arduino implements SerialPortEventListener {
	SerialPort serialPort;

        private boolean connected;
        /** definizione della porta del PC da usare. */
	private static final String PORT_NAMES[] = {
			"/dev/tty.usbserial-A700dZP9", // Mac OS X
			"/dev/ttyUSB0", // Linux
			"COM15", // Windows
			};
	/** Buffered input stream from the port */
	private InputStream input;
	/** The output stream to the port */
	private OutputStream output;
	/** Milliseconds to block while waiting for port open */
	private static final int TIME_OUT = 2000;
	/** Default bits per second for COM port. */
	private static final int DATA_RATE = 9600;
        private static CommPortIdentifier currPortId;
        private static CommPortIdentifier portId;
        private String comandi;

	public void initialize() {
                this.setConnected(false);
                System.out.println("Avviato");
		portId = null;
		Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

		// iterate through, looking for the port
		while (portEnum.hasMoreElements()) {
			currPortId = (CommPortIdentifier) portEnum.nextElement();
			for (String portName : PORT_NAMES) {
				if (currPortId.getName().equals(portName)) {
                                        portId = currPortId;
                                        this.setConnected(true);
					break;
				}
			}
		}

		if (portId == null) {
			System.out.println("Porta COM non trovata.");
                        this.setConnected(false);
			return;
		}

                }

		
	/**
	 * This should be called when you stop using the port.
	 * This will prevent port locking on platforms like Linux.
	 */
	public synchronized void close() {
		if (serialPort != null) {
			serialPort.removeEventListener();
			serialPort.close();
		}
	}

        public synchronized void connect(){


                 try {  // open serial port, and use class name for the appName.
			serialPort = (SerialPort) portId.open(this.getClass().getName(),
					TIME_OUT);

			// set port parameters
			serialPort.setSerialPortParams(DATA_RATE,
					SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1,
					SerialPort.PARITY_NONE);

			// open the streams
			input = serialPort.getInputStream();
			output = serialPort.getOutputStream();

			// add event listeners
			serialPort.addEventListener(this);
			serialPort.notifyOnDataAvailable(true);
		} catch (Exception e) {
			System.err.println(e.toString());
		}
	}

	/**
	 * Handle an event on the serial port. Read the data and print it.
	 */
	public synchronized void serialEvent(SerialPortEvent oEvent) {
		if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
			try {
				int available = input.available();
				byte chunk[] = new byte[available];
				input.read(chunk, 0, available);
                                
				// Displayed results are codepage dependent
				System.out.print(new String(chunk));
                                
			} catch (Exception e) {
				System.err.println(e.toString());
			}
		}
		// Ignore all the other eventTypes, but you should consider the other ones.
	}
    public synchronized void scrivi(String text){

        byte comando[];
        comando=text.getBytes();
        System.out.println(text);
        try{
            output.flush();
            output.write(comando);

        }catch (Exception e) {
            System.err.println(e.toString());
        }

    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public String getComandi() {
        return comandi;
    }

    public void setComandi(String comandi) {
        this.comandi = comandi;
    }

        

	
}