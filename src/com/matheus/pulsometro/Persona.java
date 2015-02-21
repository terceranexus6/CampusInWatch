package com.matheus.pulsometro;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.widget.TextView;

/**
 * Created by Espectro on 21/02/2015.
 */

public class Persona {

    private int edad;
    private int peso;
    private int altura;
    private Activity a;

    //Constructor de la clase en le caso de que el usuario no introduzca datos
    public Persona(){

        edad = 20;
        peso = 65;
        altura = 165;


    }

    //Constructor de la clase normal si el usuario introduce datos
    public Persona(int auxEdad, int auxPeso, int auxAltura, Activity a){

        edad = auxEdad;
        peso = auxPeso;
        altura = auxAltura;
        this.a=a;


    }

    //Obtiene la edad de la persona
    public int getEdad(){
        return edad;
    }

    //Obtiene la altura de la persona
    public int getAltura(){
        return altura;
    }

    //Obtiene el peso de la persona
    public int getPeso(){
        return peso;
    }

    //Obtiene la media de pulsaciones a partir de 60 valores diferentes
    public double getMedia(){

        double media = 0;

        for(int i = 0; i < 60; i++){

            media += getPulsaciones();

        }

        media = media/2;

        return media;
    }

    //Obtiene las pulsaciones de la persona
    public int getPulsaciones(){
        int pulsaciones;
        MyVars.text = (TextView) a.findViewById(R.id.text);
        pulsaciones = Integer.parseInt(MyVars.text.getText().toString());
        return pulsaciones;
    }

    //Obtiene el indice de masa corporal de la persona. Este metodo tambien podria informar a otra persona sobre el IMC del usuario
    public double IndiceCorporal(){

        double IMC;

        IMC = (getPeso())/((getAltura()/100.0));

        return IMC;
    }

    //Procesa el IMC y informa al usuario sobre su estado
    public String PesoCorporal(){

        String Resultado = "";

        if(IndiceCorporal() < 18.50){

            Resultado = "Weight to low.Eat more";

        }else if(IndiceCorporal() < 25 && IndiceCorporal() > 18.5){

             Resultado = "You have a corect weight";

            }else if(IndiceCorporal() >= 25){

                 Resultado = "You have excesive weight.";

                }else if(IndiceCorporal() >= 40)

                    Resultado = "You must to decrease your weight";

                {

        }

        return Resultado;
    }

    //Procesa la media de pulsaciones e informa sobre el resultado. Estas mediciones deben de ser tomadas cuando estas sentado o tumbado
    //Sin haber realizado un ejercicio fisico en al menos 5-10 minutos.
    public String EstadoDeSalud(){

        String Informacion;

        if(getMedia() > 85){

            Informacion = "You could have a infectious disease.";

        }else{

            Informacion = "It´s Okey";

        }

        return Informacion;
    }

    //Futuras implementaciones.

    //Si la media de las pulsaciones es menor de 40 (O un valor establecido) o mayor de 190(o un valor establecido) llamar a emergencias.
    //Este metodo se debe de utilizar siempre que se mida las pulsaciones de una persona.



    public void ComprobacionDeEmergencia(){

        if(getMedia() <= 40 || getMedia() >= 185 ){

            // LLamar a emergencias
            String url = a.getString(R.string.emergencias);
            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(url));
            a.startActivity(intent);


        }


    }







}
