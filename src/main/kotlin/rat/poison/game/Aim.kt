package rat.poison.game

import com.badlogic.gdx.math.MathUtils.clamp
import org.jire.kna.float
import rat.poison.curSettings
import rat.poison.game.CSGO.csgoEXE
import rat.poison.game.entity.Player
import rat.poison.game.entity.position
import rat.poison.game.entity.punch
import rat.poison.game.netvars.NetVarOffsets.vecViewOffset
import rat.poison.utils.Angle
import rat.poison.utils.Vector
import rat.poison.utils.generalUtil.toInt
import rat.poison.utils.randBoolean
import rat.poison.utils.randDouble
import java.lang.Math.toDegrees
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.sqrt

fun getCalculatedAngle(player: Player, dst: Vector): Angle {
	val ang = Angle()
	
	val myPunch = player.punch()
	val myPosition = player.position()
	
	val dX = myPosition.x - dst.x
	val dY = myPosition.y - dst.y
	val dZ = myPosition.z + csgoEXE.float(player + vecViewOffset) - dst.z
	myPosition.release()
	
	val hyp = sqrt((dX * dX) + (dY * dY))
	
	val rcsXVariation = curSettings.double["AIM_RCS_VARIATION"]
	val rcsYVariation = curSettings.double["AIM_RCS_VARIATION"]
	
	if (curSettings.bool["FACTOR_RECOIL"]) {
		if (curSettings.bool["AIM_ADVANCED"]) {
			val randX = if (rcsXVariation > 0F) randDouble(0.0, rcsXVariation).toFloat() * randBoolean().toInt() else 0F
			val randY = if (rcsYVariation > 0F) randDouble(0.0, rcsYVariation).toFloat() * randBoolean().toInt() else 0F
			val calcX = toDegrees(atan(dZ / hyp).toDouble()) - myPunch.x * clamp(1F + curSettings["AIM_RCS_Y"].toFloat() + randX, 1F, 2F)
			val calcY = toDegrees(atan(dY / dX).toDouble()) - myPunch.y * clamp(1F + curSettings["AIM_RCS_X"].toFloat() + randY, 1F, 2F)
			ang.x = calcX.toFloat()
			ang.y = calcY.toFloat()
		} else {
			ang.x = (toDegrees(atan(dZ / hyp).toDouble()) - myPunch.x * 2.0).toFloat() //Move these above IFs
			ang.y = (toDegrees(atan(dY / dX).toDouble()) - myPunch.y * 2.0).toFloat()
		}
	} else {
		ang.x = toDegrees(atan(dZ / hyp).toDouble()).toFloat()//Move these above IFs
		ang.y = toDegrees(atan(dY / dX).toDouble()).toFloat()
	}
	
	ang.z = 0F
	if (dX >= 0.0) ang.y += 180
	
	myPunch.release()
	
	
	ang.normalize()
	
	return ang
}

fun realCalcAngle(player: Player, dst: Vector): Angle {
	val playerPos = player.position()
	val delta = Vector(dst.x - playerPos.x, dst.y - playerPos.y, dst.z - playerPos.z + csgoEXE.float(player + vecViewOffset))
	playerPos.release()
	val myPunch = player.punch()
	
	var aX = toDegrees(atan2(-delta.z, sqrt(delta.x*delta.x + delta.y*delta.y)).toDouble())
	var aY = toDegrees(atan2(delta.y, delta.x).toDouble())
	delta.release()
	
	val rcsXVariation = curSettings.double["AIM_RCS_VARIATION"]
	val rcsYVariation = curSettings.double["AIM_RCS_VARIATION"]
	
	if (curSettings.bool["FACTOR_RECOIL"]) {
		if (curSettings.bool["AIM_ADVANCED"]) {
			val randX = if (rcsXVariation > 0.0) randDouble(0.0, rcsXVariation) * randBoolean().toInt() else 0.0
			val randY = if (rcsYVariation > 0.0) randDouble(0.0, rcsYVariation) * randBoolean().toInt() else 0.0
			val calcX = myPunch.x * clamp(1.0 + curSettings.double["AIM_RCS_Y"] + randX, 1.0, 2.0)
			val calcY = myPunch.y * clamp(1.0 + curSettings.double["AIM_RCS_X"] + randY, 1.0, 2.0)
			aX -= calcX
			aY -= calcY
		} else {
			aX -= myPunch.x * 2F
			aY -= myPunch.y * 2F
		}
	} //else don't factor
	myPunch.release()
	
	val ang = Angle(aX.toFloat(), aY.toFloat(), 0F)
	ang.normalize()
	
	return ang
}