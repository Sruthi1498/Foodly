package com.example.foodly.adapters

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.foodly.R
import com.example.foodly.util.Connection
import com.google.android.material.appbar.AppBarLayout
import com.example.foodly.activities.MainActivity
import com.example.foodly.activities.OrderPlaceActivity

import com.example.foodly.database.CartData

import com.example.foodly.database.Cart_Items
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception
import java.util.HashMap

class OrderAdapter(val context:Context,val btn:Button,val res_name:TextView,val progress_bar:RelativeLayout,val order_placed:ConstraintLayout,val appbar:AppBarLayout,val user_id:String):RecyclerView.Adapter<OrderAdapter.View_Holder>() {
    var total = 0
    val jsonArr = JSONArray()
    var list:List<Cart_Items> = CartData(context,
        Cart_Items(0,"","","",""),"getall").execute().get() as List<Cart_Items>
    class View_Holder(view: View) : RecyclerView.ViewHolder(view) {
        val name = view.findViewById<TextView>(R.id.ord_item_item_name)
        val price = view.findViewById<TextView>(R.id.ord_item_item_price)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): View_Holder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.single_item, parent, false)
        res_name.setText("Ordering From "+list[0].itemRes)
        return View_Holder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: View_Holder, position: Int) {
        val data = list[position]
        holder.name.text = data.itemName
        holder.price.text = "??? " + data.itemPrice + "/-"
        total+=data.itemPrice.toInt()
        btn.text = "Place Order ( Total : "+total+")"
        val dataOBj = JSONObject()
        dataOBj.put("food_item_id",data.item_id)
        jsonArr.put(dataOBj)
        btn.setOnClickListener {
            btn.setBackgroundResource(R.color.grey)
            Handler().postDelayed(Runnable {
                if(Connection().checkConnectivity(context)) {
                    val q = Volley.newRequestQueue(context)
                    val url = "http://13.235.250.119/v2/place_order/fetch_result/"
                    try{
                        progress_bar.visibility = View.VISIBLE
                        val jsonObj = JSONObject()
                        jsonObj.put("user_id",user_id)
                        jsonObj.put("restaurant_id",data.itemResId)
                        jsonObj.put("total_cost",total.toString())
                        jsonObj.put("food",jsonArr)
                        val jsonreq = object : JsonObjectRequest(
                            Request.Method.POST,url,jsonObj,
                            Response.Listener {
                                if(it.getJSONObject("data").getBoolean("success")){
                                    appbar.visibility = View.GONE
                                    btn.visibility = View.GONE
                                    order_placed.visibility = View.VISIBLE
                                    order_placed.findViewById<Button>(R.id.ord_ok).setOnClickListener {
                                        order_placed.findViewById<Button>(R.id.ord_ok).setBackgroundResource(R.color.white)
                                        Handler().postDelayed(Runnable {
                                            if(CartData(context, Cart_Items(0,"","","",""),"deleteall").execute().get() as Boolean){
                                                order_placed.visibility = View.GONE
                                                appbar.visibility = View.VISIBLE
                                                order_placed.findViewById<Button>(R.id.ord_ok).setBackgroundResource(R.color.red)
                                                context.startActivity(Intent(context, MainActivity::class.java))
                                            }
                                        },200)
                                    }
                                }
                            },
                            Response.ErrorListener {
                                progress_bar.visibility = View.GONE
                                context.startActivity(Intent(context, OrderPlaceActivity::class.java))
                                Toast.makeText(context,"Please Try Again Later.", Toast.LENGTH_SHORT).show()
                            }){
                            override fun getHeaders(): MutableMap<String, String> {
                                val headers = HashMap<String, String>()
                                headers["Content-type"] = "application/json"
                                headers["token"] = "c3acf1e14c21f9"
                                return headers
                            }
                        }
                        q.add(jsonreq)
                    }catch (e: Exception){
                        progress_bar.visibility = View.GONE
                        context.startActivity(Intent(context, OrderPlaceActivity::class.java))
                        Toast.makeText(context,"Please Try Again Later.", Toast.LENGTH_SHORT).show()
                    }
                }
                else{
                    Toast.makeText(context,"Please Check your Internet Connection", Toast.LENGTH_SHORT).show()
                    context.startActivity(Intent(context, OrderPlaceActivity::class.java))
                }
                btn.setBackgroundResource(R.color.red)
            }
                ,200)

        }
    }
}
