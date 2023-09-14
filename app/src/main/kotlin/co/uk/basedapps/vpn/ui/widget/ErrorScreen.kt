package co.uk.basedapps.vpn.ui.widget

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.uk.basedapps.vpn.R
import co.uk.basedapps.vpn.ui.theme.BasedAppColor

@Composable
fun ErrorScreen(
  paddingValues: PaddingValues = PaddingValues(),
  title: String = "Something went wrong",
  description: String = "Please check your Internet connection and try again later",
  isLoading: Boolean = false,
  onButtonClick: (() -> Unit)? = null,
) {
  Column(
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .padding(paddingValues)
      .fillMaxSize(),
  ) {
    Icon(
      painter = painterResource(R.drawable.ic_error),
      modifier = Modifier.size(64.dp),
      tint = BasedAppColor.Accent,
      contentDescription = null,
    )
    Spacer(modifier = Modifier.size(20.dp))
    Text(
      text = title,
      fontSize = 24.sp,
      textAlign = TextAlign.Center,
      color = BasedAppColor.TextPrimary,
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(modifier = Modifier.size(20.dp))
    Text(
      text = description,
      fontSize = 14.sp,
      textAlign = TextAlign.Center,
      color = BasedAppColor.TextSecondary,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 48.dp),
    )
    Spacer(modifier = Modifier.size(32.dp))
    if (onButtonClick != null) {
      BasedButton(
        text = "Try again",
        style = ButtonStyle.Primary,
        onClick = onButtonClick,
        size = ButtonSize.M,
        isLoading = isLoading,
      )
    }
  }
}

@Preview
@Composable
fun ErrorScreenPreview() {
  ErrorScreen(
    onButtonClick = {},
  )
}
