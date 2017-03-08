package com.huangxueqin.rookieweibo.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.huangxueqin.rookieweibo.R;
import com.huangxueqin.rookieweibo.cons.WeiboPattern;
import com.huangxueqin.rookieweibo.weiboViewModel.WeiboLinkHandler;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

/**
 * Created by huangxueqin on 2017/3/3.
 */

public class StatusTextView extends TextView {
    private int mLinkColor;
    private WeiboLinkHandler mLinkHandler;

    public StatusTextView(Context context) {
        this(context, null);
    }

    public StatusTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StatusTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.StatusTextView);
        mLinkColor = ta.getColor(R.styleable.StatusTextView_linkColor, 0xFF636463);
        ta.recycle();
        setMovementMethod(LinkMovementMethod.getInstance());
    }

    public void setLinkHandler(WeiboLinkHandler handler) {
        mLinkHandler = handler;
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(formatText(text), type);
    }

    private SpannableStringBuilder formatText(CharSequence charSequence) {
        SpannableStringBuilder ssb = new SpannableStringBuilder(charSequence);
        Linkify.addLinks(ssb, WeiboPattern.PATTERN_TOPIC, WeiboPattern.SCHEME_TOPIC);
        Linkify.addLinks(ssb, WeiboPattern.PATTERN_URL, WeiboPattern.SCHEME_URL);
        Linkify.addLinks(ssb, WeiboPattern.PATTER_AT, WeiboPattern.SCHEME_AT);
        final URLSpan[] urlSpans = ssb.getSpans(0, ssb.length(), URLSpan.class);
        for (final URLSpan span : urlSpans) {
            ClickableSpan clickableSpan = new StatusClickSpan(span.getURL());
            final int start = ssb.getSpanStart(span);
            final int end = ssb.getSpanEnd(span);
            ssb.removeSpan(span);
            if (span.getURL().startsWith(WeiboPattern.SCHEME_URL)) {
                Drawable linkIcon = new IconicsDrawable(getContext())
                        .icon(GoogleMaterial.Icon.gmd_link)
                        .color(mLinkColor)
                        .sizePx((int) getTextSize());
                ImageSpan imageSpan = new ImageSpan(linkIcon, ImageSpan.ALIGN_BASELINE);
                SpannableStringBuilder replaced = new SpannableStringBuilder("  网页链接");
                replaced.setSpan(imageSpan, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                ssb.replace(start, end, replaced);
                ssb.setSpan(clickableSpan, start, start+replaced.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                ssb.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        return ssb;
    }

    private class StatusClickSpan extends ClickableSpan {
        String spanURL;

        public StatusClickSpan(String spanURL) {
            this.spanURL = spanURL;
        }

        @Override
        public void onClick(View widget) {
            if (mLinkHandler == null) return;
            if (spanURL.startsWith(WeiboPattern.SCHEME_URL)) {
                mLinkHandler.handleURL(spanURL.substring(WeiboPattern.SCHEME_URL.length()));
            } else if (spanURL.startsWith(WeiboPattern.SCHEME_AT)) {
                mLinkHandler.handleAT(spanURL.substring(WeiboPattern.SCHEME_AT.length()));
            } else if (spanURL.startsWith(WeiboPattern.SCHEME_TOPIC)) {
                mLinkHandler.handleTopic(spanURL.substring(WeiboPattern.SCHEME_TOPIC.length())
                        .substring(1, spanURL.length()-1));
            }
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            ds.setColor(mLinkColor);
            ds.setTypeface(Typeface.DEFAULT_BOLD);
            ds.setUnderlineText(false);
        }
    }
}
