
package voicedomotica;

import edu.cmu.sphinx.frontend.util.*;
import edu.cmu.sphinx.jsapi.JSGFGrammar;
import edu.cmu.sphinx.recognizer.*;
import edu.cmu.sphinx.result.*;
import edu.cmu.sphinx.util.props.*;

import java.io.*;
import java.net.URL;

public class SpeechRec implements ResultListener, StateListener, Runnable {
	private RecognitionEventListener _rel;
	private static SpeechRec _instance = null;
	private JSGFGrammar jsgfGrammarManager;
	Recognizer recognizer;
	Microphone microphone;
        private static String risultato;
        public static boolean run;
        private static Arduino ard;

	public static SpeechRec init(String grammar) {
		if (_instance == null) {
			_instance = new SpeechRec(grammar);
		}
		return _instance;
	}

	public void setRecognitionEventListener(RecognitionEventListener rel) {
		_rel = rel;
	}

	public synchronized void newResult(Result result) {
		String resultText = result.getBestFinalResultNoFiller();
		
                if(!resultText.isEmpty()){
                    System.out.println("Parlato riconosciuto: " + resultText + "\n");
                    resultText=resultText+"!";
                    this.setRisultato(resultText);
                    this.ard.scrivi(resultText);
                }
                else
                    System.out.println("Parlato non riconosciuto, per favore ripeti\n");
		if (_rel != null) {
			_rel.newEvent(resultText);
		}
	}

	public void statusChanged(RecognizerState s) {
		System.out.println("Stato: " + s);
	}

        public SpeechRec(Arduino arduino){
            this.run=false;
            this.ard=arduino;
        }

	private SpeechRec(String grammar) {
              
		try {
                        this.ard.connect();
			URL url;
			url = SpeechRec.class.getResource("speechrec.config.xml");
			System.out.println("Caricamento di Sphinx...");

			ConfigurationManager cm = new ConfigurationManager(url);

			recognizer = (Recognizer) cm.lookup("recognizer");
			microphone = (Microphone) cm.lookup("microphone");
			jsgfGrammarManager = (JSGFGrammar) cm.lookup("jsgfGrammar");

			recognizer.addResultListener(this);
			recognizer.addStateListener(this);

			/* allocate the resource necessary for the recognizer */
			recognizer.allocate();
			System.out.println("Sphinx Caricato Con Successo");

			/* the microphone will keep recording until the program exits */
			if (microphone.startRecording()) {
				jsgfGrammarManager.loadJSGF(grammar);
				new Thread(this).start();
			} else {
				System.out.println("Impossibile avviare il microfono.");
				recognizer.deallocate();
				System.exit(1);
			}
		} catch (IOException e) {
			System.err.println("Errore durante il caricamento : " + e);
			e.printStackTrace();
		} catch (PropertyException e) {
			System.err.println("Errore durante la configurazione : " + e);
			e.printStackTrace();
		} catch (InstantiationException e) {
			System.err.println("Errore durante la creazione : " + e);
			e.printStackTrace();
		}
	}

	public void run() {
                this.run=true;
		while (this.isRun()) {
			recognizer.recognize();
		}
                
                recognizer.deallocate();
                _instance = null;
                microphone.stopRecording();
                
        }
                

	public void start() {
	    
            SpeechRec.init("default");
		
	}

        public void stop(Arduino arduino) {

            this.run=false;
            arduino.close();

        }

    public boolean isRun() {
        return this.run;
    }

    public static String getRisultato() {
        return risultato;
    }

    public static void setRisultato(String risultato) {
        SpeechRec.risultato = risultato;
    }

    

}
