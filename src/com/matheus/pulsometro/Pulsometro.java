package com.matheus.pulsometro;

import java.util.concurrent.atomic.AtomicBoolean;

//Tono y vibracion
import android.media.AudioManager;
import com.matheus.pulsometro.MyVars.TYPE;

import android.media.Image;
import android.os.Vibrator;
import android.media.ToneGenerator;
//import com.matheus.pulsometro.Browser;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

@SuppressLint("NewApi")


public class Pulsometro extends Activity {

    private Button imageButton;


    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("deprecation")
    @Override

    //Funcion de java que se llama justo cuando se inicia la aplicacion
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

          setContentView(R.layout.main );


            final ImageButton boton = (ImageButton) findViewById(R.id.imageButton2);

            boton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    boton.setVisibility(View.GONE);
                    MyVars.viewFinder.setVisibility(View.VISIBLE);


                }
            });

            //Aqui creamos y mantenemos la preview con surferview
                MyVars.viewFinder = (SurfaceView) findViewById(R.id.preview);
                MyVars.viewFinderHolder = MyVars.viewFinder.getHolder();
                 MyVars.viewFinderHolder.addCallback(surfaceCallback);

                //Esta funcion en necesaria ya que surface te obliga a crear los buffers
                MyVars.viewFinderHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

                //Muestra la imagen de la camara y el numero de pulsaciones
                MyVars.image = findViewById(R.id.image);
                MyVars.text = (TextView) findViewById(R.id.text);//Confirmado que muestra el numero de pulsaciones

                //Manejo de la bataria del reloj para un minimo consumo
                PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                MyVars.wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "DoNotDimScreen");



              /*
              * Debe de existir un protocolo de comprobacion para que si nada mas llamar al onCreate no se detecta nada
              * con la camara se lance una excepcions y se cierre la camara.
              * Para evitar esto se podria llamar a preview(la camara) junto con el menu y hacerle invisible
              * Es una idea. No tengo sueño pero si no duerme creo que mañana seguire escribindome a mi mismo
              *
              * */

    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    /**
     * {@inheritDoc}
     */
    @Override

    //Funcion de Android que recupera la aplicacion en caso de interrupcion
    public void onResume() {
        super.onResume();
        //if(MyVars.camera != null) {

            MyVars.wakeLock.acquire();
            MyVars.camera = Camera.open();
            MyVars.startTime = System.currentTimeMillis();
       // }
    }

    /**
     * {@inheritDoc}
     */
    @Override

    //Funcion de la aplicacion  para parar la aplicacion
    public void onPause() {
        super.onPause();

       //if(MyVars.camera != null){

           MyVars.wakeLock.release();

           MyVars.camera.setPreviewCallback(null);
           MyVars.camera.stopPreview();
           MyVars.camera.release();
           MyVars.camera = null;


       //}

    }

    

    private PreviewCallback previewCallback = new PreviewCallback() {

        /**
         * {@inheritDoc}
         */
        @Override
        public void onPreviewFrame(byte[] data, Camera cam) {
            if (data == null) throw new NullPointerException();
            Camera.Size size = cam.getParameters().getPreviewSize();
            if (size == null) throw new NullPointerException();

            if (!MyVars.processing.compareAndSet(false, true)) return;

            int width = size.width;
            int height = size.height;

            int imgAvg = Fotopletismografia.redAVG(data.clone(), height, width);
            // Log.i(TAG, "imgAvg="+imgAvg);
            if (imgAvg == 0 || imgAvg == 255) {
                MyVars.processing.set(false);
                return;
            }

            int arrayAVGsum = 0;
            int arrayAVGcount = 0;
            //Aqui se hace una media de pulsaciones y se guarda en la clase MyVars
            for (int i = 0; i < MyVars.arrayAVG.length; i++) {
                if (MyVars.arrayAVG[i] >= 1) {
                    arrayAVGcount++;
                    arrayAVGsum += MyVars.arrayAVG[i];
                }
            }

            int changeAVG = (arrayAVGcount > 0) ? (arrayAVGsum / arrayAVGcount) : 0;
            TYPE newType = MyVars.statusBatimento;
            if (imgAvg < changeAVG) {
                newType = TYPE.beatON;
                if (newType != MyVars.statusBatimento) {
                    MyVars.beats++;
                }
            } else if (imgAvg > changeAVG) {
                newType = TYPE.beatOFF;
            }

            if (MyVars.indexAVG == MyVars.arraySizeAVG) MyVars.indexAVG = 0;
            MyVars.arrayAVG[MyVars.indexAVG] = imgAvg;
            MyVars.indexAVG++;

            // Transitioned from one state to another to the same
            if (newType != MyVars.statusBatimento) {
                MyVars.statusBatimento = newType;
                MyVars.image.postInvalidate();
            }

            long endTime = System.currentTimeMillis();
            double totalTimeInSecs = (endTime - MyVars.startTime) / 1000d;
            if (totalTimeInSecs >= 10) {
                ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_SYSTEM, ToneGenerator.MAX_VOLUME);
                toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP);
                Vibrator vibs = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vibs.vibrate(600);
                double bps = (MyVars.beats / totalTimeInSecs);
                int dpm = (int) (bps * 60d);
                if (dpm < 30 || dpm > 180) {
                    MyVars.startTime = System.currentTimeMillis();
                    int beats = (int)MyVars.beats;
                    MyVars.beats = 0;
                    MyVars.processing.set(false);

                    // Browser browser = new Browser();
                    // browser.callBrowser(beats);

                    return;
                }

                // Log.d(TAG,
                // "totalTimeInSecs="+totalTimeInSecs+" beats="+beats);

                if (MyVars.beatsIndex == MyVars.beatsArraySize) MyVars.beatsIndex = 0;
                MyVars.beatsArray[MyVars.beatsIndex] = dpm;
                MyVars.beatsIndex++;

                int beatsArrayAvg = 0;
                int beatsArrayCnt = 0;
                for (int i = 0; i < MyVars.beatsArray.length; i++) {
                    if (MyVars.beatsArray[i] > 0) {
                        beatsArrayAvg += MyVars.beatsArray[i];
                        beatsArrayCnt++;
                    }
                }
                int beatsAvg = (beatsArrayAvg / beatsArrayCnt);
                MyVars.text.setText(String.valueOf(beatsAvg));
                MyVars.startTime = System.currentTimeMillis();
                MyVars.beats = 0;


            }


            MyVars.processing.set(false);
        }
    };

   //De aqui pa abajo parecen xcepciones del programa

    private SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {

        /**
         * {@inheritDoc}
         */
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                MyVars.camera.setPreviewDisplay(MyVars.viewFinderHolder);
                MyVars.camera.setPreviewCallback(previewCallback);
            } catch (Throwable t) {
                Log.e("PreviewDemo-surfaceCallback", "Exception in setPreviewDisplay()", t);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            Camera.Parameters parameters = MyVars.camera.getParameters();
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            Camera.Size size = getSmallestPreviewSize(width, height, parameters);
            if (size != null) {
                parameters.setPreviewSize(size.width, size.height);
                Log.d(MyVars.APP, "Using width=" + size.width + " height=" + size.height);
            }
            MyVars.camera.setParameters(parameters);
            MyVars.camera.startPreview();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            // Ignore
        }
    };

    @SuppressLint("NewApi")
    private static Camera.Size getSmallestPreviewSize(int width, int height, Camera.Parameters parameters) {
        Camera.Size result = null;

        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
            if (size.width <= width && size.height <= height) {
                if (result == null) {
                    result = size;
                } else {
                    int resultArea = result.width * result.height;
                    int newArea = size.width * size.height;

                    if (newArea < resultArea) result = size;
                }
            }
        }

        return result;
    }
}
