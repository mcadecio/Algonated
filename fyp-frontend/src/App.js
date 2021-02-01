import React from 'react';
import './App.css';
import {BrowserRouter as Router, Link, Redirect, Route, Switch} from 'react-router-dom';
import AuthProvider from './components/auth/AuthContext';
import AuthNav from './components/auth/AuthButton';
import LoginPage from './components/home/login/LoginPage';
import {ScalesExercise} from './components/exercise/scales/ScalesExercise';
import {TSPExercise} from './components/exercise/tsp/TSPExercise';
import Animations from './components/Animations';
import Navbar from 'react-bootstrap/Navbar';
import Nav from 'react-bootstrap/Nav';
import ExerciseView from './components/exercise/ExerciseView';
import ReactPlayer from 'react-player/file';
import {ResponsiveNetworkAnimation} from './components/exercise/tsp/TSPAnimations';

function App() {

    return (
        <AuthProvider>
            <Router>
                <div style={{marginBottom: '5%'}}>
                    <Navbar variant={'dark'} expand="lg" style={{backgroundColor: '#00325b'}} fixed={'top'}>
                        <Navbar.Brand>Algorithms</Navbar.Brand>
                        <Nav className="mr-auto">
                            <Nav.Link as={Link} to={'/exercises'}>Exercises</Nav.Link>
                        </Nav>
                        <AuthNav/>
                    </Navbar>
                </div>
                <div style={{margin: '3%'}}>
                    <Switch>
                        <Route exact path={'/'}>
                            <Redirect to={'/exercises'}/>
                        </Route>
                        <Route exact path="/login" component={LoginPage}/>
                        <Route exact path="/exercises/animation" component={Animations}/>
                        <Route exact path="/exercises/tspanimation">
                            <ResponsiveNetworkAnimation solutions={[]} solution={[]} weights={[[]]} test={true}/>
                        </Route>
                        <Route exact path='/exercises' component={ExerciseView}/>
                        <Route exact path="/exercises/scales" component={ScalesExercise}/>
                        <Route exact path="/exercises/tsp" component={TSPExercise}/>
                        <Route path="*">
                            <br/>
                            <div className={'d-flex justify-content-center'}>
                                <ReactPlayer
                                    playing={true}
                                    muted={true}
                                    url={'https://cdn.dribbble.com/users/5139370/screenshots/15008550/media/a9b073c175dd3d5d93c2c681da6a311e.mp4'}
                                    loop={true}
                                    controls={false}
                                />
                            </div>
                        </Route>
                    </Switch>
                </div>
            </Router>
        </AuthProvider>
    );
}

export default App;


