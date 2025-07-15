package co.uk.basedapps.vpn.ui.widget

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RandomServerRow(onClick: () -> Unit) {
  Row(
    modifier = Modifier
      .clickable(onClick = onClick)
      .heightIn(min = 60.dp)
      .padding(16.dp)
      .fillMaxWidth(),
  ) {
    Text(
      text = "Quick connect!",
      overflow = TextOverflow.Ellipsis,
      maxLines = 1,
      fontSize = 18.sp,
      fontWeight = FontWeight.Bold,
    )
  }
}
