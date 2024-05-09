package com.example.renter_g11.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.renter_g11.R
import com.example.renter_g11.models.Property

class WatchListAdapter(
    var watchList: List<Property>,
    var deleteFunctionFromViewListingsActivity: (String) -> Unit,
    var goToPropertyDetails: (Int) -> Unit

) : RecyclerView.Adapter<WatchListAdapter.WatchListViewHolder>() {

    inner class WatchListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WatchListViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.watchlist_row_layout, parent, false)
        return WatchListViewHolder(view)
    }

    override fun getItemCount(): Int {
        return watchList.size
    }

    override fun onBindViewHolder(holder: WatchListViewHolder, position: Int) {
        val currItem: Property = watchList.get(position)

        val ivUrl = holder.itemView.findViewById<ImageView>(R.id.ivUrl)
        val tvAddress = holder.itemView.findViewById<TextView>(R.id.tvAddress)
        val tvRentalType = holder.itemView.findViewById<TextView>(R.id.tvRentalType)
        val tvNumBedroom = holder.itemView.findViewById<TextView>(R.id.tvNumBedroom)
        val tvRentalPrice = holder.itemView.findViewById<TextView>(R.id.tvRentalPrice)

        tvAddress.text = currItem.address
        tvRentalType.text = currItem.rentalType
        tvNumBedroom.text = "${currItem.numberOfBedrooms}-BHK"
        tvRentalPrice.text = "$${currItem.rentalPrice}"

        Glide.with(holder.itemView.context).load(currItem.imageUrl).into(ivUrl)

        val ivDelete = holder.itemView.findViewById<ImageView>(R.id.ivDelete)
        ivDelete.setOnClickListener {
            deleteFunctionFromViewListingsActivity(currItem.id)
        }

        holder.itemView.setOnClickListener {
            goToPropertyDetails(position)
        }
    }
}