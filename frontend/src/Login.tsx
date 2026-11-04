import { FormEvent, useState } from 'react'
import { Command, LockKeyhole, Mail, ArrowRight, Sparkles, UserRound } from 'lucide-react'
import { login, register } from './api'

export default function Login({onLogin}:{onLogin:()=>void}) {
 const [mode,setMode]=useState<'signin'|'signup'>('signin')
 const [name,setName]=useState('')
 const [email,setEmail]=useState('')
 const [password,setPassword]=useState('')
 const [error,setError]=useState('')
 const [success,setSuccess]=useState('')
 const [loading,setLoading]=useState(false)
 async function submit(e:FormEvent){
  e.preventDefault();setError('');setSuccess('');setLoading(true)
  try{
   if(mode==='signup'){
    await register(name.trim(),email.trim(),password)
    setMode('signin')
    setSuccess('Account created. Sign in with your new email and password.')
    setName('')
   }else{
    await login(email.trim(),password)
    onLogin()
   }
  }catch{
   setError(mode==='signup'?'Could not create your account. This email may already be registered; check your details and try again.':'We couldn’t sign you in. Check your email and password, then try again.')
  }finally{setLoading(false)}
 }
 return <main className="login-screen"><div className="login-art"><div className="login-brand"><span className="brand-mark"><Command size={19}/></span><b>dreamsync</b></div><div className="login-message"><span className="login-kicker"><Sparkles size={14}/> YOUR WORK, IN SYNC</span><h1>Make space for<br/><em>great work.</em></h1><p>Bring your projects, people and plans together. Less chasing updates, more moving forward.</p><div className="login-art-card"><div className="mini-line"/><div className="mini-board"><span/><span/><span/></div><div className="mini-caption">A clearer way to work, together.</div></div></div><div className="login-footer">Thoughtful collaboration starts here.</div></div><div className="login-panel"><form className="login-form" onSubmit={submit}><span className="eyebrow">{mode==='signin'?'WELCOME BACK':'JOIN DREAMSYNC'}</span><h2>{mode==='signin'?'Sign in to DreamSync':'Create your account'}</h2><p>{mode==='signin'?'Enter your details to access your workspace.':'Create an account to collaborate on a project.'}</p>{mode==='signup'&&<label className="field-label">Full name<div className="login-input"><UserRound size={17}/><input value={name} onChange={e=>setName(e.target.value)} placeholder="Your name" required/></div></label>}<label className="field-label">Email address<div className="login-input"><Mail size={17}/><input type="email" value={email} onChange={e=>setEmail(e.target.value)} placeholder="you@company.com" required/></div></label><label className="field-label">Password<div className="login-input"><LockKeyhole size={17}/><input type="password" value={password} onChange={e=>setPassword(e.target.value)} placeholder={mode==='signup'?'At least 6 characters':'Enter your password'} minLength={mode==='signup'?6:undefined} required/></div></label>{error&&<div className="login-error">{error}</div>}{success&&<div className="login-success">{success}</div>}<button className="primary-btn login-submit" disabled={loading}>{loading?(mode==='signup'?'Creating account...':'Signing in...'):(mode==='signup'?'Create account':'Sign in')} <ArrowRight size={16}/></button><button type="button" className="auth-mode-toggle" onClick={()=>{setMode(mode==='signin'?'signup':'signin');setError('');setSuccess('')}}>{mode==='signin'?"New to DreamSync? Create an account":"Already have an account? Sign in"}</button>{mode==='signin'&&<div className="login-hint">Use an account already registered in your DreamSync database.</div>}</form><div className="login-panel-footer">© 2026 DreamSync · Built for teams that move together</div></div></main>
}
