package name.hergeth.util;

public class IntField {
    int[] ints;

    public IntField(String s){
        String regex = "\\W+";
        // Aufteilen an Wortgrenzen
        String[] stde = s.split(regex);
        ints = new int[stde.length];
        for(int i = 0; i < stde.length; i++){
            ints[i] = Integer.parseInt(stde[i]);
        }
    }

    public boolean contains(int j){
        for(int i = 0; i < ints.length; i++){
            if(ints[i] == j){
                return true;
            }
        }
        return false;
    }
}
