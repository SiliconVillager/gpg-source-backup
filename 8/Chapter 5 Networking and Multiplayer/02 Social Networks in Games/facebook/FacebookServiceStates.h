//         Facebook Demo for Game Programming Gems 8 by Claus Höfele          //
//==============================================================================

#ifndef WEB_FACEBOOK_FACEBOOKSERVICESTATES_H
#define WEB_FACEBOOK_FACEBOOKSERVICESTATES_H

//==============================================================================
// Declarations
//==============================================================================

namespace FacebookServiceStates
{
  /** Status codes for the Facebook service. */
  enum FacebookServiceState
  {
    Ok = 0,

    NetworkError,               ///< Unspecified problem with network.
    PermissionsError,           ///< No permission to execute request.
    SessionError,               ///< Valid session could not be established.
    XmlError                    ///< Unspecified error when parsing XML.
  };
}

typedef FacebookServiceStates::FacebookServiceState FacebookServiceState;

#endif // WEB_FACEBOOK_FACEBOOKSERVICESTATES_H
