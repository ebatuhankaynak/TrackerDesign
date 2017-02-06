package ebk.trackerDesign.model;

import ebk.trackerDesign.R;

/**
 * Created by E.Batuhan Kaynak on 30.6.2016.
 */
public class TrackerActivityType {
    public static String EDUCATION = "Education";
    public static String ENTERTAINMENT = "Entertainment";
    public static String SPORTS = "Sports";
    public static String SLEEP = "Sleep";

    public static String[] allActivities = {
            EDUCATION,
            ENTERTAINMENT,
            SPORTS,
            SLEEP
    };
    public static String[] allTodos = {
            EDUCATION,
            ENTERTAINMENT,
            SPORTS
    };

    public static Integer setDrawable(String type){
        Integer drawableId = null;
        if (type.equals(TrackerActivityType.EDUCATION)) {
            drawableId = R.drawable.education;
        } else if (type.equals(TrackerActivityType.ENTERTAINMENT)) {
            drawableId = R.drawable.entertainment;
        } else if (type.equals(TrackerActivityType.SPORTS)){
            drawableId = R.drawable.sports;
        }else if (type.equals(TrackerActivityType.SLEEP)){
            drawableId = R.drawable.sleep;
        }
        return drawableId;
    }

    public static String getActivityType(String radioString){
        String result = "";
        for (int i = 0; radioString.charAt(i) != '_'; i++){
            if (i == 0){
                result = result + radioString.substring(0, 1).toUpperCase();
            }else {
                result = result + radioString.charAt(i);
            }
        }
        return result;
    }

    public static String getActivityType(int radioPosistion){
        String result = "";
        switch(radioPosistion){
            case 0:
                result = EDUCATION;
                break;
            case 1:
                result = ENTERTAINMENT;
                break;
            case 2:
                result = SPORTS;
                break;
            case 3:
                result = SLEEP;
                break;
        }
        return result;
    }

    public static Integer getTodoType(String type){
        Integer drawableId = null;
        if (type.equals(TrackerActivityType.EDUCATION)) {
            drawableId = R.drawable.education_todo;
        } else if (type.equals(TrackerActivityType.ENTERTAINMENT)) {
            drawableId = R.drawable.entertainment_todo;
        } else if (type.equals(TrackerActivityType.SPORTS)) {
            drawableId = R.drawable.sports_todo;
        }
        return drawableId;
    }
}
