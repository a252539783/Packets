package com.iqiyi.liquanfei_sx.vpnt.text;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.text.Layout;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.AlignmentSpan;
import android.text.style.LeadingMarginSpan;
import android.text.style.ParagraphStyle;
import android.util.Log;

/**
 * Created by Administrator on 2017/10/30.
 *
 * SimpleFixedLayout makes all character occupy a fixed width when draw.
 * Here 'character' means the every 1-byte char in a unicode string.So a character
 * may occupy 2 or more space a 1-byte char occupy.
 */

public class SimpleFixedLayout extends Layout{
    public static final int ENCODE_HEX=1;

    private int mEncode=-1;
    private int mOneWidth,mOneHeight;
    private float mTextDesc,mTextSc;
    private int mColumns;
    private int mLineCount;

    private int mSelectedStart=40,mSelectedEnd=90;
    private int mCursorLength=3;

    private byte[] mSource=null;

    private AddedInput mInputs=null;

    private boolean mSelecting=false,mStartSelected=false;

    private static boolean sInited=false;

    private static Directions DIRS_ALL_LEFT_TO_RIGHT;

    private static Paint sSelectedText,sSelectedBg,sSSelectedBg;

    static
    {
        DIRS_ALL_LEFT_TO_RIGHT = null;
    }

    private static void sInit(TextPaint p)
    {

        if (!sInited)
        {
            sInited=true;
            sSelectedBg=new Paint(p);
            sSelectedText=new Paint(p);
            sSSelectedBg=new Paint(p);

            sSelectedText.setColor(Color.WHITE);
            sSelectedBg.setColor(Color.parseColor("#2196F3"));
            sSSelectedBg.setColor(Color.BLUE);

            ByteConstants.initHex();
        }


    }

    private void init(TextPaint paint)
    {
        mOneHeight= (int)(paint.getFontMetrics().bottom-paint.getFontMetrics().top);
        sSelectedText.setTextSize(paint.getTextSize());
        sSSelectedBg.setTextSize(paint.getTextSize());
        sSelectedBg.setTextSize(paint.getTextSize());
        mTextDesc=paint.getFontMetrics().descent;
        mTextSc=paint.getFontMetrics().ascent;
    }

    /**
     * Subclasses of Layout use this constructor to set the display text,
     * width, and other standard properties.
     *
     * @param text        the text to render
     * @param paint       the default paint for the layout.  Styles can override
     *                    various attributes of the paint.
     * @param width       the wrapping width for the text.
     * @param align       whether to left, right, or center the text.  Styles can
     *                    override the alignment.
     * @param spacingMult factor by which to scale the font size to get the
     *                    default line spacing
     * @param spacingAdd  amount to add to the default line spacing
     */
    public SimpleFixedLayout(CharSequence text, TextPaint paint, int width, Alignment align, float spacingMult, float spacingAdd) {
        super(text, paint, width, align, spacingMult, spacingAdd);
        mEncode=-1;
        sInit(paint);
        init(paint);
    }

    public SimpleFixedLayout(byte[] src, TextPaint paint, int width, Alignment align, float spacingMult, float spacingAdd) {
        super("aaaaa", paint, width, align, spacingMult, spacingAdd);
        mEncode=ENCODE_HEX;
        mSource=src;
        sInit(paint);
        init(paint);

        mInputs=new AddedInput(src);
    }

    public int getOneHeight()
    {
        return mOneHeight;
    }

    public void setOne(int w,int h)
    {
        mOneWidth =w;
        if (h!=-1)
            mOneHeight=h;
        mColumns=(int)(getWidth()/ mOneWidth);
        if (mColumns!=0)
        {
            if (mEncode==ENCODE_HEX)
                mLineCount=mSource.length/mColumns+1;
            else
                mLineCount=getText().length()/mColumns+1;
        }
    }

    public void select(int index)
    {
        if (index<0||index>getCharCount())
            return ;

        if (mSelecting)
        {
            if (mStartSelected)
            {
                if (index>mSelectedEnd)
                {
                    mStartSelected=false;
                    mSelectedStart=mSelectedEnd;
                }else
                {
                    mSelectedStart=index;
                }

            }else
            {
                if (index<mSelectedStart)
                {
                    mStartSelected=true;
                    mSelectedEnd=mSelectedStart;
                }else {
                    mSelectedEnd = index;
                }
            }
        }else
        {
            mSelecting=true;
            if (mSelectedStart<index&&mSelectedEnd>index)
            {
                if (index-mSelectedStart>mSelectedEnd-index){
                    mSelectedEnd=index;
                    mStartSelected=false;
                }
                else {
                    mSelectedStart = index;
                    mStartSelected=true;
                }
            }else
            {
                mSelectedStart=mSelectedEnd=index;
            }
        }
    }

    public void resetSelect()
    {
        mSelectedStart=mSelectedEnd=-1;
    }

    public boolean isSelect(int index)
    {
        return index>=mSelectedStart&&index<=mSelectedEnd;
    }

    public void stopSelect()
    {
        mSelecting=false;
    }

    public void drawText(Canvas canvas, int firstLine, int lastLine) {
        if (firstLine!=0)
            firstLine--;

        if (lastLine!=getLineCount())
            lastLine++;

        int index=mColumns*firstLine;
        float drawY=(firstLine+1)*mOneHeight;
        if (mEncode==ENCODE_HEX)
        {
            for (int i=firstLine;i<lastLine;i++)
            {
                int x=0;
                for (int j=getLineStart(i);j<mColumns;j++)
                {
                    if (index==mInputs.getCharCount())
                        break;
                    if (index>=mSelectedStart&&index<mSelectedEnd)
                    {
                        canvas.drawRect(x,drawY-mOneHeight,x+mOneWidth,drawY,sSelectedBg);
                        canvas.drawText(ByteConstants.HEX[mInputs.getByte(index++)&0xff],0,2,x,drawY-mTextDesc,sSelectedText);
                    }else
                    {
                        if (index==mSelectedStart)
                        {
                            canvas.drawRect(x,drawY-mOneHeight,x+3,drawY,sSelectedBg);
                        }
                        canvas.drawText(ByteConstants.HEX[mInputs.getByte(index++)&0xff],0,2,x,drawY-mTextDesc,getPaint());
                    }
                    x+=mOneWidth;
                }
                drawY+=mOneHeight;
            }
        }else
        {
            for (int i=firstLine;i<lastLine;i++)
            {
                int x=0;
                for (int j=getLineStart(i);j<mColumns;j++)
                {
                    if (index==getText().length())
                        break;
                    if (index>=mSelectedStart&&index<mSelectedEnd)
                    {
                        canvas.drawRect(x,drawY-mOneHeight,x+mOneWidth,drawY,sSelectedBg);
                        canvas.drawText(((String)getText()),index++,index,x,drawY-mTextDesc,sSelectedText);
                    }else
                    {
                        if (index==mSelectedStart)
                        {
                            canvas.drawRect(x,drawY-mOneHeight,x+3,drawY,sSelectedBg);
                        }
                        canvas.drawText(((String)getText()),index++,index,x,drawY-mTextDesc,getPaint());
                    }
                    x+=mOneWidth;
                }
                drawY+=mOneHeight;
            }
        }
    }

    public int getCharCount()
    {
        return mInputs.getCharCount();
    }

    @Override
    public void draw(Canvas canvas, Path highlight, Paint highlightPaint, int cursorOffsetVertical) {
        super.draw(canvas, highlight, highlightPaint, cursorOffsetVertical);
    }

    @Override
    public int getLineForVertical(int vertical) {
        return vertical/mOneHeight;
    }

    @Override
    public int getLineCount() {
        return mLineCount;
    }

    @Override
    public int getLineTop(int line) {
        return line*mOneHeight;
    }

    @Override
    public int getLineDescent(int line) {
        return 0;
    }

    @Override
    public int getLineStart(int line) {
        return 0;
    }

    @Override
    public int getParagraphDirection(int line) {
        return DIR_LEFT_TO_RIGHT;
    }

    @Override
    public boolean getLineContainsTab(int line) {
        return false;
    }

    @Override
    public Directions getLineDirections(int line) {
        return DIRS_ALL_LEFT_TO_RIGHT;
    }

    @Override
    public int getTopPadding() {
        return 0;
    }

    @Override
    public int getBottomPadding() {
        return 0;
    }

    @Override
    public int getEllipsisStart(int line) {
        return Integer.MAX_VALUE;
    }

    @Override
    public int getEllipsisCount(int line) {
        return 0;
    }
}
