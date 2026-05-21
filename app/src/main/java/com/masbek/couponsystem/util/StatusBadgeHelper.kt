package com.masbek.couponsystem.util

import android.content.res.ColorStateList
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView
import com.masbek.couponsystem.R

object StatusBadgeHelper {

    fun applyStatus(textView: TextView, cardView: MaterialCardView?, status: String) {
        val context = textView.context

        val (bgColor, textColor, displayText) = when (status.lowercase()) {
            "draft" -> Triple(
                ContextCompat.getColor(context, R.color.status_draft_bg),
                ContextCompat.getColor(context, R.color.status_draft_text),
                "Draft"
            )
            "generating", "in_progress" -> Triple(
                ContextCompat.getColor(context, R.color.status_progress_bg),
                ContextCompat.getColor(context, R.color.status_progress_text),
                if (status == "generating") "Generating" else "In Progress"
            )
            "ready", "completed" -> Triple(
                ContextCompat.getColor(context, R.color.status_complete_bg),
                ContextCompat.getColor(context, R.color.status_complete_text),
                if (status == "ready") "Ready" else "Completed"
            )
            "in_production" -> Triple(
                ContextCompat.getColor(context, R.color.status_production_bg),
                ContextCompat.getColor(context, R.color.status_production_text),
                "In Production"
            )
            "pending" -> Triple(
                ContextCompat.getColor(context, R.color.status_draft_bg),
                ContextCompat.getColor(context, R.color.status_draft_text),
                "Pending"
            )
            else -> Triple(
                ContextCompat.getColor(context, R.color.status_draft_bg),
                ContextCompat.getColor(context, R.color.status_draft_text),
                status.replaceFirstChar { it.uppercase() }
            )
        }

        textView.text = displayText
        textView.setTextColor(textColor)

        cardView?.let {
            it.setCardBackgroundColor(ColorStateList.valueOf(bgColor))
            it.strokeWidth = 0
        } ?: run {
            textView.setBackgroundColor(bgColor)
        }
    }

    fun applyPrizeBadge(textView: TextView, cardView: MaterialCardView?, amount: Int) {
        val context = textView.context

        val (bgColor, textColor) = if (amount > 0) {
            Pair(
                ContextCompat.getColor(context, R.color.status_complete_bg),
                ContextCompat.getColor(context, R.color.status_complete_text)
            )
        } else {
            Pair(
                ContextCompat.getColor(context, R.color.status_draft_bg),
                ContextCompat.getColor(context, R.color.status_draft_text)
            )
        }

        textView.setTextColor(textColor)
        cardView?.let {
            it.setCardBackgroundColor(ColorStateList.valueOf(bgColor))
            it.strokeWidth = 0
        }
    }
}
