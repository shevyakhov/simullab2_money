package com.chelz.simullab2_money

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.chelz.simullab2_money.databinding.ActivityMainBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.yabu.livechart.view.LiveChart
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Random

class MainActivity : AppCompatActivity() {

	var howfast = 0L
	var rubCoef = 0.0f
	var usdCoef = 0.0f
	var isStarted = false
	var c = 0.0
	var job: Job = Job()
	val random = Random()
	lateinit var chart: LiveChart
	lateinit var chart1: LineChart
	lateinit var binding: ActivityMainBinding

	var xr = 0.0f
	var yr = 0.0f
	var xu = 0.0f
	var yu = 0.0f
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityMainBinding.inflate(layoutInflater)
		chart1 = binding.chart1
		chart = binding.chart
		setContentView(binding.root)

		var xAxis: XAxis
		run {
			xAxis = binding.chart1.xAxis
			xAxis.mAxisMinimum = 0f
			xAxis.enableGridDashedLine(10f, 10f, 0f)
		}

		binding.btRedo.setOnClickListener {
			if (job.isActive) {
				if (!isStarted) {
					isStarted = true
					binding.btStart.text = getString(R.string.stop)
				}
				job.cancel("")
				CoroutineScope(Dispatchers.Default).launch {
					predict()
				}
			} else
				CoroutineScope(Dispatchers.Default).launch {
					predict()
				}
		}
		binding.btStart.setOnClickListener {
			changeState()
		}

	}

	private fun changeState() {
		isStarted = !isStarted
		if (isStarted) {
			binding.btStart.text = getString(R.string.stop)
		} else {
			binding.btStart.text = getString(R.string.start)
		}
	}

	private suspend fun predict() = coroutineScope {
		howfast = binding.edHowfast.text.toString().toLong()
		rubCoef = binding.edRubdiff.text.toString().toFloat()
		usdCoef = binding.edUsddiff.text.toString().toFloat()
		xr = 0.0f
		xu = 0.0f
		yr = binding.edStartingRateRub.text.toString().toFloat()
		yu = binding.edStartingRateUsd.text.toString().toFloat()
		try {
			val rubDataset1 = LineDataSet(mutableListOf(Entry(xr, yr)), "rub").apply {
				val color: Int = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256))
				this.color = color
				setCircleColor(color)
				circleRadius = 1f
				lineWidth = 3F
			}
			val usdDataset1 = LineDataSet(mutableListOf(Entry(xu, yu)), "usd").apply {
				val color: Int = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256))
				this.color = color
				setCircleColor(color)
				circleRadius = 1f
				lineWidth = 3F
			}


			binding.chart1.data = LineData(listOf(rubDataset1, usdDataset1))



			job = launch {
				var i = 1
				while (i < binding.edDays.text.toString().toInt()) {
					if (isStarted && isActive) {
						delay(howfast)
						withContext(Dispatchers.Main) {
							chart1.nextRate(i.toFloat())
						}
						i++
					}
				}
			}


		} catch (e: Exception) {
			println(e.message)
		}
	}

	private fun LineChart.nextRate(index: Float) {
		yr *= (1 + rubCoef * (random.nextFloat() - 0.5f))
		yu *= (1 + usdCoef * (random.nextFloat() - 0.5f))
		this.data.dataSets.first().addEntry(Entry(index + 1, yr))
		this.data.dataSets.last().addEntry(Entry(index + 1, yu))
		this.data = LineData(this.data.dataSets)
		animateXY(0, 0)
	}
}