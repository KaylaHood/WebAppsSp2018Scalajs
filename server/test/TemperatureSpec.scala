import org.scalatestplus.play.PlaySpec
import models.TempData
import org.scalatestplus.play._
import play.api.test.Helpers._
import controllers.TempController
import play.api.test.FakeRequest

class TemperatureSpec extends PlaySpec {
  "TempData model" should {
    "provide 30 values" in {
      val td = new TempData("data/SanAntonioTemps.csv")
      val data = td.getMonth(4, 1973)
      data.length must be (30)
    }
    
    "match month and year" in {
      val td = new TempData("data/SanAntonioTemps.csv")
      val data = td.getMonth(4, 1973)
      data.isEmpty must be (false)
      data.forall(_.month == 4) must be (true)
      data.forall(_.year == 1973) must be (true)
    }
  }
  
}