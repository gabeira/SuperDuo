package barqsoft.footballscores;

import android.content.Context;
import android.graphics.drawable.PictureDrawable;
import android.net.Uri;
import android.widget.ImageView;

import com.bumptech.glide.GenericRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.StreamEncoder;
import com.bumptech.glide.load.resource.file.FileToStreamDecoder;
import com.caverock.androidsvg.SVG;

import java.io.InputStream;

import barqsoft.footballscores.svg.SvgDecoder;
import barqsoft.footballscores.svg.SvgDrawableTranscoder;
import barqsoft.footballscores.svg.SvgSoftwareLayerSetter;

/**
 * Created by yehya khaled on 3/3/2015.
 */
public class Utility {

    public static final int BUNDESLIGA1 = 394;
    public static final int BUNDESLIGA2 = 395;
    public static final int LIGUE1 = 396;
    public static final int LIGUE2 = 397;
    public static final int PREMIER_LEAGUE = 398;
    public static final int PRIMERA_DIVISION = 399;
    public static final int SEGUNDA_DIVISION = 400;
    public static final int SERIE_A = 401;
    public static final int PRIMERA_LIGA = 402;
    public static final int BUNDESLIGA3 = 403;
    public static final int EREDIVISIE = 404;
    public static final int CHAMPIONS_LEAGUE = 405;

    public static String getLeague(Context context, int league_num) {
        switch (league_num) {
            case SERIE_A:
                return context.getString(R.string.league_seriea);
            case LIGUE1:
                return context.getString(R.string.league_ligue1);
            case LIGUE2:
                return context.getString(R.string.league_ligue2);
            case PRIMERA_LIGA:
                return context.getString(R.string.league_primera_liga);
            case PREMIER_LEAGUE:
                return context.getString(R.string.league_premier_league);
            case CHAMPIONS_LEAGUE:
                return context.getString(R.string.league_champions_league);
            case PRIMERA_DIVISION:
                return context.getString(R.string.league_primera_division);
            case SEGUNDA_DIVISION:
                return context.getString(R.string.league_segunda_division);
            case BUNDESLIGA1:
                return context.getString(R.string.league_bundesliga1);
            case BUNDESLIGA2:
                return context.getString(R.string.league_bundesliga2);
            case BUNDESLIGA3:
                return context.getString(R.string.league_bundesliga3);
            case EREDIVISIE:
                return context.getString(R.string.league_eredivisie);
            default:
                return context.getString(R.string.league_not_found);
        }
    }

    public static String getMatchDay(Context context, int match_day, int league_num) {
        if (league_num == CHAMPIONS_LEAGUE) {
            if (match_day <= 6) {
                return context.getString(R.string.group_stage_text) + ", " + context.getString(R.string.matchday_text) + " : 6";
            } else if (match_day == 7 || match_day == 8) {
                return context.getString(R.string.first_knockout_round);
            } else if (match_day == 9 || match_day == 10) {
                return context.getString(R.string.quarter_final);
            } else if (match_day == 11 || match_day == 12) {
                return context.getString(R.string.semi_final);
            } else {
                return context.getString(R.string.final_text);
            }
        } else {
            return context.getString(R.string.matchday_text) + " : " + String.valueOf(match_day);
        }
    }

    public static String getScores(int home_goals, int awaygoals) {
        if (home_goals < 0 || awaygoals < 0) {
            return " - ";
        } else {
            return String.valueOf(home_goals) + " - " + String.valueOf(awaygoals);
        }
    }

    public static int getTeamCrestByTeamName(String teamname) {
        if (teamname == null) {
            return R.drawable.no_icon;
        }
        switch (teamname) { //This is the set of icons that are currently in the app. Feel free to find and add more
            //as you go.
            case "Arsenal London FC":
                return R.drawable.arsenal;
            case "Manchester United FC":
                return R.drawable.manchester_united;
            case "Swansea City":
                return R.drawable.swansea_city_afc;
            case "Leicester City":
                return R.drawable.leicester_city_fc_hd_logo;
            case "Everton FC":
                return R.drawable.everton_fc_logo1;
            case "West Ham United FC":
                return R.drawable.west_ham;
            case "Tottenham Hotspur FC":
                return R.drawable.tottenham_hotspur;
            case "West Bromwich Albion":
                return R.drawable.west_bromwich_albion_hd_logo;
            case "Sunderland AFC":
                return R.drawable.sunderland;
            case "Stoke City FC":
                return R.drawable.stoke_city;
            default:
                return R.drawable.no_icon;
        }
    }

    private static GenericRequestBuilder<Uri, InputStream, SVG, PictureDrawable> requestBuilder;
    /**
     * Load Team Crest with Glide and SVG
     *
     * @param imageView The reference to Image View to load into.
     * @param crestUrl The URL which the Image come from.
     * @param context A Context object which should be some mock instance.
     */
    public static void setTeamCrestIntoImageView(ImageView imageView, String crestUrl, Context context){
        if (crestUrl == null){
            imageView.setImageResource(R.drawable.no_icon);
        } else
        if(crestUrl.toLowerCase().endsWith("svg")){
            requestBuilder =
                    Glide.with(context)
                            .using(Glide.buildStreamModelLoader(Uri.class, context), InputStream.class)
                            .from(Uri.class)
                            .as(SVG.class)
                            .transcode(new SvgDrawableTranscoder(), PictureDrawable.class)
                            .sourceEncoder(new StreamEncoder())
                            .cacheDecoder(new FileToStreamDecoder<SVG>(new SvgDecoder()))
                            .decoder(new SvgDecoder())
                            .error(R.drawable.no_icon)
                            .animate(android.R.anim.fade_in)
                            .listener(new SvgSoftwareLayerSetter<Uri>());

            Uri uri = Uri.parse(crestUrl);
            requestBuilder.diskCacheStrategy(DiskCacheStrategy.SOURCE)// SVG cannot be serialized so it's not worth to cache it
                    .load(uri)
                    .error(R.drawable.no_icon)
                    .animate(android.R.anim.fade_in)
                    .into(imageView);
        }else{
            Glide.with(context)
                    .load(crestUrl)
                    .fitCenter()
                    .error(R.drawable.no_icon)
                    .animate(android.R.anim.fade_in)
                    .into(imageView);
        }
    }
}
