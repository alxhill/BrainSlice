package com.lemonslice.brainslice;

import android.text.Html;
import android.widget.TextView;

/**
 * Created by James on 31/01/14.
 *
 * working:
 * getSmallCaps(TextView tv)
 *
 * not working:
 * getSmallCaps(String s)
 */
public class Utils {

    //get small caps code to convert to small caps (not yet working)
    private static char[] smallCaps = new char[]
            {
                    '\uf761', //A
                    '\uf762',
                    '\uf763',
                    '\uf764',
                    '\uf765',
                    '\uf766',
                    '\uf767',
                    '\uf768',
                    '\uf769',
                    '\uf76A',
                    '\uf76B',
                    '\uf76C',
                    '\uf76D',
                    '\uf76E',
                    '\uf76F',
                    '\uf770',
                    '\uf771',
                    '\uf772',
                    '\uf773',
                    '\uf774',
                    '\uf775',
                    '\uf776',
                    '\uf777',
                    '\uf778',
                    '\uf779',
                    '\uf77A'   //Z
            };


    //get small caps by simply returning small caps string (not yet working)
    public static String getSmallCaps(String s)
    {
        char[] chars = s.toCharArray();
        for(int i = 0; i < chars.length; i++)
        {
            if(chars[i] >= 'a' && chars[i] <= 'z')
            {
                chars[i] = smallCaps[chars[i] - 'a'];
            }
        }
        return String.valueOf(chars);
    }


    //get small caps by passing and returning textview (working)
    public static TextView getSmallCaps(TextView tv)
    {
        String s = tv.getText().toString();
        tv.setText(
                Html.fromHtml(s.substring(0, 1)
                        + "<small>"
                        + s.substring(1, s.length())
                        + "</small>")
        );

        return tv;
    }
}
